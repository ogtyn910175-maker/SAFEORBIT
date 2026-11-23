const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

const PROJECT_ID = 'safeorbit-387ee';

describe('SafeOrbit Firestore Security & Privacy Rules Audit', () => {
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

  // ──────────────────────────────────────────────────────────
  // 1. UNAUTHENTICATED ACCESS (Zero-Trust Perimeter)
  // ──────────────────────────────────────────────────────────
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

  // ──────────────────────────────────────────────────────────
  // 2. PRIVATE USER PROFILES (users/{userId}) - PII PROTECTION
  // ──────────────────────────────────────────────────────────
  describe('2. Private User Profile Security (Owner-Only Access)', () => {
    it('allows a user to read, create and update their own private profile', async () => {
      const db = testEnv.authenticatedContext('user_1').firestore();
      await assertSucceeds(db.collection('users').doc('user_1').set({
        id: 'user_1',
        name: 'User One',
        email: 'user1@safeorbit.com',
        emergencyPhone: '+91 99999 88888',
        role: 'Creator',
        isProfileSetupComplete: true
      }));

      // Owner reading own private profile
      await assertSucceeds(db.collection('users').doc('user_1').get());

      // Owner updating own private profile
      await assertSucceeds(db.collection('users').doc('user_1').update({
        name: 'User One Updated',
        emergencyPhone: '+91 77777 66666'
      }));
    });

    it('denies any other user (including family members and outsiders) from reading another users private profile', async () => {
      const user1Db = testEnv.authenticatedContext('user_1').firestore();
      await user1Db.collection('users').doc('user_1').set({
        id: 'user_1',
        name: 'User One Private',
        email: 'private_email@safeorbit.com',
        emergencyPhone: '+91 12345 67890'
      });

      // User 2 attempts to read User 1's profile -> MUST BE DENIED
      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').get());
    });

    it('denies another user from modifying a private profile', async () => {
      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').update({
        name: 'Hacked by User 2'
      }));
    });
  });

  // ──────────────────────────────────────────────────────────
  // 3. FAMILY CIRCLES (circles/{circleId})
  // ──────────────────────────────────────────────────────────
  describe('3. Family Circle Creation & Boundary Security', () => {
    it('allows authenticated user to create a circle where they are owner and member', async () => {
      const alice = testEnv.authenticatedContext('alice').firestore();
      await assertSucceeds(alice.collection('circles').doc('circle_alice').set({
        id: 'circle_alice',
        name: 'Alice Family Orbit',
        ownerId: 'alice',
        inviteCode: 'SO-ALICE-123',
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
        inviteCode: 'SO-FAKE-123',
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
        inviteCode: 'SO-PRIV-123',
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
        inviteCode: 'SO-SHARE-123',
        memberIds: ['alice', 'bob']
      });

      await assertSucceeds(alice.collection('circles').doc('circle_shared').get());

      const bob = testEnv.authenticatedContext('bob').firestore();
      await assertSucceeds(bob.collection('circles').doc('circle_shared').get());
    });
  });

  // ──────────────────────────────────────────────────────────
  // 4. PRIVILEGE ESCALATION & CIRCLE METADATA PROTECTION
  // ──────────────────────────────────────────────────────────
  describe('4. Privilege Escalation & Member Leaving Controls', () => {
    before(async () => {
      const alice = testEnv.authenticatedContext('alice_p').firestore();
      await alice.collection('circles').doc('circle_priv').set({
        id: 'circle_priv',
        name: 'Priv Test Circle',
        ownerId: 'alice_p',
        inviteCode: 'SO-PRIV-TEST',
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

    it('denies a member from kicking out other members (only owner can)', async () => {
      // Re-add bob for this test
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

  // ──────────────────────────────────────────────────────────
  // 5. SEPARATE LIMITED MEMBER DISPLAY DOCUMENTS (NO PII)
  // ──────────────────────────────────────────────────────────
  describe('5. Limited Member Display Cards (/circles/{id}/members/{id})', () => {
    before(async () => {
      const alice = testEnv.authenticatedContext('alice_s').firestore();
      await alice.collection('circles').doc('circle_sub').set({
        id: 'circle_sub',
        name: 'Sub Test Orbit',
        ownerId: 'alice_s',
        inviteCode: 'SO-SUB-TEST',
        memberIds: ['alice_s', 'bob_s']
      });
      // Limited display document: contains name, role, avatarInitial, NO email, NO phone
      await alice.collection('circles').doc('circle_sub').collection('members').doc('alice_s').set({
        id: 'alice_s',
        userId: 'alice_s',
        name: 'Alice S',
        role: 'Owner',
        avatarInitial: 'A',
        isSharingActive: false
      });
      await alice.collection('circles').doc('circle_sub').collection('members').doc('bob_s').set({
        id: 'bob_s',
        userId: 'bob_s',
        name: 'Bob S',
        role: 'Member',
        avatarInitial: 'B',
        isSharingActive: false
      });
    });

    it('allows verified circle members to read member display cards', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      const card = await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').get()
      );
      // Verify display card contains name and avatar, and NO PII
      const data = card.data();
      if (data) {
        if (data.email) throw new Error('PII Leak: Member display card must not expose email');
        if (data.emergencyPhone) throw new Error('PII Leak: Member display card must not expose emergencyPhone');
      }
    });

    it('denies outsider from reading member display cards', async () => {
      const charlie = testEnv.authenticatedContext('charlie_s').firestore();
      await assertFails(
        charlie.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').get()
      );
    });

    it('allows member to update their own display card', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            name: 'Bob S Updated'
          })
      );
    });

    it('denies member from updating another members display card', async () => {
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

  // ──────────────────────────────────────────────────────────
  // 6. INVITATION REDEMPTION & JOINING VALIDATION
  // ──────────────────────────────────────────────────────────
  describe('6. Invitation Expiry & Authorized Circle Joining', () => {
    const inviteCode = 'SO-JOIN-999';
    const circleId = 'circle_join_test';

    before(async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      // Create circle with inviteCode
      await owner.collection('circles').doc(circleId).set({
        id: circleId,
        name: 'Joinable Circle',
        ownerId: 'owner_j',
        inviteCode: inviteCode,
        memberIds: ['owner_j']
      });
      // Create valid active invite
      await owner.collection('invites').doc(inviteCode).set({
        code: inviteCode,
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() + (24 * 60 * 60 * 1000) // valid 24h
      });
    });

    it('allows creator to create an invite valid up to 48 hours', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await assertSucceeds(owner.collection('invites').doc('SO-VALID-48').set({
        code: 'SO-VALID-48',
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() + (47 * 60 * 60 * 1000)
      }));
    });

    it('denies creating an invite with expiry exceeding 48 hours', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await assertFails(owner.collection('invites').doc('SO-INVALID-72').set({
        code: 'SO-INVALID-72',
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() + (72 * 60 * 60 * 1000)
      }));
    });

    it('denies reading an expired invite', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await owner.collection('invites').doc('SO-EXPIRED').set({
        code: 'SO-EXPIRED',
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() - (60 * 1000) // expired
      });

      const member = testEnv.authenticatedContext('member_j').firestore();
      await assertFails(member.collection('invites').doc('SO-EXPIRED').get());
    });

    it('denies reading a deactivated invite', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await owner.collection('invites').doc('SO-DEACTIVATED').set({
        code: 'SO-DEACTIVATED',
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: false, // inactive
        expiresAt: Date.now() + (24 * 60 * 60 * 1000)
      });

      const member = testEnv.authenticatedContext('member_j').firestore();
      await assertFails(member.collection('invites').doc('SO-DEACTIVATED').get());
    });

    it('allows a user to join a circle when a valid active invite exists', async () => {
      const member = testEnv.authenticatedContext('member_j').firestore();

      // Step 1: Member creates their limited display card
      await assertSucceeds(
        member.collection('circles').doc(circleId)
          .collection('members').doc('member_j').set({
            id: 'member_j',
            userId: 'member_j',
            name: 'New Member',
            role: 'Member',
            avatarInitial: 'N'
          })
      );

      // Step 2: Member joins by adding self to memberIds
      await assertSucceeds(
        member.collection('circles').doc(circleId).update({
          memberIds: ['owner_j', 'member_j']
        })
      );
    });

    it('denies an unauthorized user from joining a circle when NO active invite exists', async () => {
      const owner = testEnv.authenticatedContext('owner_no_inv').firestore();
      const privateCircleId = 'circle_no_invite';

      // Circle created with an expired or non-existent invite
      await owner.collection('circles').doc(privateCircleId).set({
        id: privateCircleId,
        name: 'Private No Invite',
        ownerId: 'owner_no_inv',
        inviteCode: 'SO-NON-EXISTENT',
        memberIds: ['owner_no_inv']
      });

      const attacker = testEnv.authenticatedContext('attacker').firestore();

      // Attacker attempts to add self without a valid active invite in the invites collection -> DENIED
      await assertFails(
        attacker.collection('circles').doc(privateCircleId).update({
          memberIds: ['owner_no_inv', 'attacker']
        })
      );
    });
  });
});
