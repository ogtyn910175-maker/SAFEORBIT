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

  // 1. Unauthenticated Access
  describe('1. Unauthenticated Access (Zero-Trust Gate)', () => {
    it('denies unauthenticated read from users/{userId}', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      await assertFails(db.collection('users').doc('user_anon').get());
    });

    it('denies unauthenticated write to users/{userId}', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      await assertFails(db.collection('users').doc('user_anon').set({ name: 'Hacker' }));
    });

    it('denies unauthenticated read from circles/{circleId}', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      await assertFails(db.collection('circles').doc('circle_anon').get());
    });

    it('denies unauthenticated read from invites/{code}', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      await assertFails(db.collection('invites').doc('SO-000-000').get());
    });
  });

  // 2. User Profiles
  describe('2. User Profile Security', () => {
    it('allows a user to create and update their own profile', async () => {
      const db = testEnv.authenticatedContext('user_1').firestore();
      await assertSucceeds(db.collection('users').doc('user_1').set({
        id: 'user_1',
        name: 'User One',
        role: 'Creator',
        isProfileSetupComplete: true
      }));

      await assertSucceeds(db.collection('users').doc('user_1').update({
        name: 'User One Updated'
      }));
    });

    it('denies a user from modifying another users profile', async () => {
      const user1Db = testEnv.authenticatedContext('user_1').firestore();
      await assertSucceeds(user1Db.collection('users').doc('user_1').set({
        id: 'user_1',
        name: 'User One'
      }));

      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').update({
        name: 'Hacked by User 2'
      }));
    });

    it('allows authenticated users to read profiles to display member names', async () => {
      const user1Db = testEnv.authenticatedContext('user_1').firestore();
      await user1Db.collection('users').doc('user_1').set({ id: 'user_1', name: 'User One' });

      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertSucceeds(user2Db.collection('users').doc('user_1').get());
    });
  });

  // 3. Family Circles
  describe('3. Family Circle Creation & Boundary Security', () => {
    it('allows authenticated user to create a circle where they are owner and member', async () => {
      const alice = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(alice.collection('circles').doc('circle_alice').set({
        id: 'circle_alice',
        name: 'Alice Family Orbit',
        ownerId: 'alice',
        memberIds: ['alice'],
        createdAt: Date.now()
      }));
    });

    it('denies user from forging circle ownership to someone else', async () => {
      const alice = testEnv.authenticatedContext('alice').firestore();
      await assertFails(alice.collection('circles').doc('circle_fake').set({
        id: 'circle_fake',
        name: 'Fake Orbit',
        ownerId: 'bob',
        memberIds: ['alice'],
        createdAt: Date.now()
      }));
    });

    it('denies an outsider from reading a private circle', async () => {
      const alice = testEnv.authenticatedContext('alice').firestore();
      await alice.collection('circles').doc('circle_private').set({
        id: 'circle_private',
        name: 'Private Circle',
        ownerId: 'alice',
        memberIds: ['alice']
      });

      const charlie = testEnv.authenticatedContext('charlie').firestore();
      await assertFails(charlie.collection('circles').doc('circle_private').get());
    });

    it('allows circle members and owner to read their circle', async () => {
      const alice = testEnv.authenticatedContext('alice').firestore();
      await alice.collection('circles').doc('circle_shared').set({
        id: 'circle_shared',
        name: 'Shared Circle',
        ownerId: 'alice',
        memberIds: ['alice', 'bob']
      });

      await assertSucceeds(alice.collection('circles').doc('circle_shared').get());

      const bob = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(bob.collection('circles').doc('circle_shared').get());
    });
  });

  // 4. Privilege Escalation & Member Leaving
  describe('4. Privilege Escalation & Controlled Membership Changes', () => {
    before(async () => {
      const alice = testEnv.authenticatedContext('alice_p').firestore();
      await alice.collection('circles').doc('circle_priv').set({
        id: 'circle_priv',
        name: 'Priv Test Circle',
        ownerId: 'alice_p',
        memberIds: ['alice_p', 'bob_p']
      });
    });

    it('allows owner to update circle metadata', async () => {
      const alice = testEnv.authenticatedContext('alice_p').firestore();
      await assertSucceeds(alice.collection('circles').doc('circle_priv').update({
        name: 'Renamed Priv Circle'
      }));
    });

    it('denies regular member from changing circle metadata or ownership', async () => {
      const bob = testEnv.authenticatedContext('bob_p').firestore();
      await assertFails(bob.collection('circles').doc('circle_priv').update({
        name: 'Hacked Priv Name'
      }));
      await assertFails(bob.collection('circles').doc('circle_priv').update({
        ownerId: 'bob_p'
      }));
    });

    it('allows a member to leave by removing strictly their own UID', async () => {
      const bob = testEnv.authenticatedContext('bob_p').firestore();
      await assertSucceeds(bob.collection('circles').doc('circle_priv').update({
        memberIds: ['alice_p']
      }));
    });

    it('denies a member from kicking out other members', async () => {
      const alice = testEnv.authenticatedContext('alice_p').firestore();
      await alice.collection('circles').doc('circle_priv').update({
        memberIds: ['alice_p', 'bob_p']
      });

      const bob = testEnv.authenticatedContext('bob_p').firestore();
      await assertFails(bob.collection('circles').doc('circle_priv').update({
        memberIds: ['bob_p']
      }));
    });
  });

  // 5. Circle Members Subcollection
  describe('5. Circle Members Subcollection Security', () => {
    before(async () => {
      const alice = testEnv.authenticatedContext('alice_s').firestore();
      await alice.collection('circles').doc('circle_sub').set({
        id: 'circle_sub',
        name: 'Sub Test Orbit',
        ownerId: 'alice_s',
        memberIds: ['alice_s', 'bob_s']
      });
      await alice.collection('circles').doc('circle_sub').collection('members').doc('alice_s').set({
        id: 'alice_s',
        name: 'Alice S',
        role: 'Owner'
      });
      await alice.collection('circles').doc('circle_sub').collection('members').doc('bob_s').set({
        id: 'bob_s',
        name: 'Bob S',
        role: 'Member'
      });
    });

    it('denies outsider from reading member cards', async () => {
      const charlie = testEnv.authenticatedContext('charlie_s').firestore();
      await assertFails(
        charlie.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').get()
      );
    });

    it('allows members to read member cards', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').get()
      );
    });

    it('allows member to update their own card', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            name: 'Bob S Updated'
          })
      );
    });

    it('denies member from updating another members card', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertFails(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').update({
            name: 'Bob Modified Alice'
          })
      );
    });

    it('allows circle owner to remove a member card', async () => {
      const alice = testEnv.authenticatedContext('alice_s').firestore();
      await assertSucceeds(
        alice.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').delete()
      );
    });
  });

  // 6. Invite Codes
  describe('6. Invite Codes Expiry & Access Control', () => {
    it('allows creator to create an invite valid up to 48 hours', async () => {
      const alice = testEnv.authenticatedContext('alice_inv').firestore();
      const now = Date.now();
      await assertSucceeds(alice.collection('invites').doc('SO-INV-VALID').set({
        code: 'SO-INV-VALID',
        circleId: 'circle_sub',
        createdBy: 'alice_inv',
        isActive: true,
        expiresAt: now + (24 * 60 * 60 * 1000)
      }));
    });

    it('denies creating an invite with expiry exceeding 48 hours', async () => {
      const alice = testEnv.authenticatedContext('alice_inv').firestore();
      const now = Date.now();
      await assertFails(alice.collection('invites').doc('SO-INV-LONG').set({
        code: 'SO-INV-LONG',
        circleId: 'circle_sub',
        createdBy: 'alice_inv',
        isActive: true,
        expiresAt: now + (72 * 60 * 60 * 1000)
      }));
    });

    it('denies reading an expired invite', async () => {
      const past = Date.now() - (60 * 1000);
      const alice = testEnv.authenticatedContext('alice_inv').firestore();
      await alice.collection('invites').doc('SO-INV-EXPIRED').set({
        code: 'SO-INV-EXPIRED',
        circleId: 'circle_sub',
        createdBy: 'alice_inv',
        isActive: true,
        expiresAt: past
      });

      const bob = testEnv.authenticatedContext('bob_inv').firestore();
      await assertFails(bob.collection('invites').doc('SO-INV-EXPIRED').get());
    });

    it('denies reading a deactivated invite', async () => {
      const future = Date.now() + (24 * 60 * 60 * 1000);
      const alice = testEnv.authenticatedContext('alice_inv').firestore();
      await alice.collection('invites').doc('SO-INV-INACT').set({
        code: 'SO-INV-INACT',
        circleId: 'circle_sub',
        createdBy: 'alice_inv',
        isActive: false,
        expiresAt: future
      });

      const bob = testEnv.authenticatedContext('bob_inv').firestore();
      await assertFails(bob.collection('invites').doc('SO-INV-INACT').get());
    });
  });
});
