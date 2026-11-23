const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

const PROJECT_ID = 'safeorbit-387ee';

describe('SafeOrbit Firestore Security Rules Audit', () => {
  let testEnv;

  before(async () => {
    const rules = fs.readFileSync(path.resolve(__dirname, 'firestore.rules'), 'utf8');
    testEnv = await initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules: rules,
        host: '127.0.0.1',
        port: 8080
      }
    });
  });

  after(async () => {
    if (testEnv) {
      await testEnv.cleanup();
    }
  });

  beforeEach(async () => {
    if (testEnv) {
      await testEnv.clearFirestore();
    }
  });

  // ──────────────────────────────────────────────────────────
  // 1. UNAUTHENTICATED ACCESS
  // ──────────────────────────────────────────────────────────
  describe('Unauthenticated Access (Zero-Trust Gate)', () => {
    it('denies unauthenticated read from users/{userId}', async () => {
      const unauthedDb = testEnv.unauthenticatedContext().firestore();
      await assertFails(unauthedDb.collection('users').doc('alice').get());
    });

    it('denies unauthenticated write to users/{userId}', async () => {
      const unauthedDb = testEnv.unauthenticatedContext().firestore();
      await assertFails(unauthedDb.collection('users').doc('alice').set({ name: 'Hacker' }));
    });

    it('denies unauthenticated read from circles/{circleId}', async () => {
      const unauthedDb = testEnv.unauthenticatedContext().firestore();
      await assertFails(unauthedDb.collection('circles').doc('circle_1').get());
    });

    it('denies unauthenticated read from invites/{code}', async () => {
      const unauthedDb = testEnv.unauthenticatedContext().firestore();
      await assertFails(unauthedDb.collection('invites').doc('SO-123-456').get());
    });
  });

  // ──────────────────────────────────────────────────────────
  // 2. USER PROFILES (users/{userId})
  // ──────────────────────────────────────────────────────────
  describe('User Profile Security', () => {
    it('allows a user to create and update their own profile', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(aliceDb.collection('users').doc('alice').set({
        id: 'alice',
        name: 'Alice',
        role: 'Creator',
        isProfileSetupComplete: true
      }));

      await assertSucceeds(aliceDb.collection('users').doc('alice').update({
        name: 'Alice Updated'
      }));
    });

    it('denies a user from modifying another users profile', async () => {
      // Set up Alice's profile via admin context
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('users').doc('alice').set({
          id: 'alice',
          name: 'Alice'
        });
      });

      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(bobDb.collection('users').doc('alice').update({
        name: 'Bob Modified Alice'
      }));
    });

    it('allows authenticated users to read profiles to display member names', async () => {
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('users').doc('alice').set({
          id: 'alice',
          name: 'Alice'
        });
      });

      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(bobDb.collection('users').doc('alice').get());
    });
  });

  // ──────────────────────────────────────────────────────────
  // 3. FAMILY CIRCLES (circles/{circleId})
  // ──────────────────────────────────────────────────────────
  describe('Family Circle Creation & Boundary Security', () => {
    it('allows authenticated user to create a circle where they are owner and member', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(aliceDb.collection('circles').doc('circle_1').set({
        id: 'circle_1',
        name: 'Alice Family Orbit',
        ownerId: 'alice',
        memberIds: ['alice'],
        createdAt: Date.now()
      }));
    });

    it('denies user from forging circle ownership to someone else', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertFails(aliceDb.collection('circles').doc('circle_fake').set({
        id: 'circle_fake',
        name: 'Fake Orbit',
        ownerId: 'bob', // Alice claims Bob is owner
        memberIds: ['alice'],
        createdAt: Date.now()
      }));
    });

    it('denies an outsider from reading a private circle', async () => {
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('circles').doc('circle_private').set({
          id: 'circle_private',
          name: 'Alice Family Orbit',
          ownerId: 'alice',
          memberIds: ['alice', 'bob']
        });
      });

      const charlieDb = testEnv.authenticatedContext('charlie').firestore();
      await assertFails(charlieDb.collection('circles').doc('circle_private').get());
    });

    it('allows circle members and owner to read their circle', async () => {
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('circles').doc('circle_private').set({
          id: 'circle_private',
          name: 'Alice Family Orbit',
          ownerId: 'alice',
          memberIds: ['alice', 'bob']
        });
      });

      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(aliceDb.collection('circles').doc('circle_private').get());

      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(bobDb.collection('circles').doc('circle_private').get());
    });
  });

  // ──────────────────────────────────────────────────────────
  // 4. PRIVILEGE ESCALATION & CIRCLE JOIN/LEAVE
  // ──────────────────────────────────────────────────────────
  describe('Privilege Escalation & Controlled Membership Changes', () => {
    beforeEach(async () => {
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('circles').doc('circle_test').set({
          id: 'circle_test',
          name: 'SafeOrbit Core',
          ownerId: 'alice',
          memberIds: ['alice', 'bob']
        });
      });
    });

    it('allows owner to update circle metadata', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(aliceDb.collection('circles').doc('circle_test').update({
        name: 'Renamed Orbit'
      }));
    });

    it('denies regular member from changing circle metadata or ownership', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(bobDb.collection('circles').doc('circle_test').update({
        name: 'Bob Hacked Orbit'
      }));

      await assertFails(bobDb.collection('circles').doc('circle_test').update({
        ownerId: 'bob'
      }));
    });

    it('allows a member to leave by removing strictly their own UID', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(bobDb.collection('circles').doc('circle_test').update({
        memberIds: ['alice'] // Bob removes only Bob
      }));
    });

    it('denies a member from kicking out other members (only owner can)', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(bobDb.collection('circles').doc('circle_test').update({
        memberIds: ['bob'] // Bob tries to kick Alice
      }));
    });
  });

  // ──────────────────────────────────────────────────────────
  // 5. CIRCLE MEMBERS SUBCOLLECTION
  // ──────────────────────────────────────────────────────────
  describe('Circle Members Subcollection (/circles/{id}/members/{id})', () => {
    beforeEach(async () => {
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('circles').doc('circle_sub').set({
          id: 'circle_sub',
          name: 'Test Orbit',
          ownerId: 'alice',
          memberIds: ['alice', 'bob']
        });
        await context.firestore().collection('circles').doc('circle_sub')
          .collection('members').doc('alice').set({
            id: 'alice',
            name: 'Alice',
            role: 'Owner'
          });
        await context.firestore().collection('circles').doc('circle_sub')
          .collection('members').doc('bob').set({
            id: 'bob',
            name: 'Bob',
            role: 'Member'
          });
      });
    });

    it('denies outsider from reading member cards', async () => {
      const charlieDb = testEnv.authenticatedContext('charlie').firestore();
      await assertFails(
        charlieDb.collection('circles').doc('circle_sub')
          .collection('members').doc('alice').get()
      );
    });

    it('allows members to read member cards', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(
        bobDb.collection('circles').doc('circle_sub')
          .collection('members').doc('alice').get()
      );
    });

    it('allows member to update their own card', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(
        bobDb.collection('circles').doc('circle_sub')
          .collection('members').doc('bob').update({
            name: 'Bob Updated'
          })
      );
    });

    it('denies member from updating another members card', async () => {
      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(
        bobDb.collection('circles').doc('circle_sub')
          .collection('members').doc('alice').update({
            name: 'Bob Modified Alice'
          })
      );
    });

    it('allows circle owner to remove a member card', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(
        aliceDb.collection('circles').doc('circle_sub')
          .collection('members').doc('bob').delete()
      );
    });
  });

  // ──────────────────────────────────────────────────────────
  // 6. INVITE CODES (invites/{code})
  // ──────────────────────────────────────────────────────────
  describe('Invite Codes Expiry & Access Control', () => {
    it('allows creator to create an invite valid up to 48 hours', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      const now = Date.now();
      await assertSucceeds(aliceDb.collection('invites').doc('SO-111-222').set({
        code: 'SO-111-222',
        circleId: 'circle_sub',
        createdBy: 'alice',
        isActive: true,
        expiresAt: now + (24 * 60 * 60 * 1000) // 24h validity
      }));
    });

    it('denies creating an invite with expiry exceeding 48 hours', async () => {
      const aliceDb = testEnv.authenticatedContext('alice').firestore();
      const now = Date.now();
      await assertFails(aliceDb.collection('invites').doc('SO-TOO-LONG').set({
        code: 'SO-TOO-LONG',
        circleId: 'circle_sub',
        createdBy: 'alice',
        isActive: true,
        expiresAt: now + (72 * 60 * 60 * 1000) // 72h is not allowed
      }));
    });

    it('denies reading an expired invite', async () => {
      const pastTime = Date.now() - (60 * 60 * 1000); // 1 hour ago
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('invites').doc('SO-EXPIRED').set({
          code: 'SO-EXPIRED',
          circleId: 'circle_sub',
          createdBy: 'alice',
          isActive: true,
          expiresAt: pastTime
        });
      });

      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(bobDb.collection('invites').doc('SO-EXPIRED').get());
    });

    it('denies reading a deactivated invite', async () => {
      const futureTime = Date.now() + (24 * 60 * 60 * 1000);
      await testEnv.withSecurityRulesDisabled(async (context) => {
        await context.firestore().collection('invites').doc('SO-INACTIVE').set({
          code: 'SO-INACTIVE',
          circleId: 'circle_sub',
          createdBy: 'alice',
          isActive: false, // deactivated
          expiresAt: futureTime
        });
      });

      const bobDb = testEnv.authenticatedContext('bob').firestore();
      await assertFails(bobDb.collection('invites').doc('SO-INACTIVE').get());
    });
  });
});
