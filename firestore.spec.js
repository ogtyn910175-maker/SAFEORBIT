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
  // 1. UNAUTHENTICATED ACCESS (Zero-Trust Gate)
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
    before(async () => {
      const user1Db = testEnv.authenticatedContext('user_1', { email: 'user1@safeorbit.com' }).firestore();
      await user1Db.collection('users').doc('user_1').set({
        id: 'user_1',
        name: 'User One',
        email: 'user1@safeorbit.com',
        emergencyPhone: '+91 99999 88888',
        role: 'Creator',
        createdAt: 1000000,
        currentCircleId: null,
        isProfileSetupComplete: true
      });
    });

    it('allows owner to read and update their own permitted fields', async () => {
      const db = testEnv.authenticatedContext('user_1').firestore();
      await assertSucceeds(db.collection('users').doc('user_1').get());
      await assertSucceeds(db.collection('users').doc('user_1').update({
        name: 'User One Updated',
        emergencyPhone: '+91 77777 66666',
        avatarInitial: 'U'
      }));
    });

    it('denies User A from changing User Bs currentCircleId', async () => {
      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').update({
        currentCircleId: 'circle_malicious'
      }));
    });

    it('denies User A from changing User Bs role', async () => {
      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').update({
        role: 'Demoted'
      }));
    });

    it('denies User A from reading User Bs private profile', async () => {
      const user2Db = testEnv.authenticatedContext('user_2').firestore();
      await assertFails(user2Db.collection('users').doc('user_1').get());
    });

    it('denies owner from tampering with immutable security fields (id, email, createdAt)', async () => {
      const user1Db = testEnv.authenticatedContext('user_1').firestore();
      await assertFails(user1Db.collection('users').doc('user_1').update({
        email: 'forged_email@safeorbit.com'
      }));
      await assertFails(user1Db.collection('users').doc('user_1').update({
        id: 'forged_id'
      }));
      await assertFails(user1Db.collection('users').doc('user_1').update({
        createdAt: 9999999
      }));
    });

    it('denies owner from setting currentCircleId to a circle they do not belong to', async () => {
      const user1Db = testEnv.authenticatedContext('user_1').firestore();
      await assertFails(user1Db.collection('users').doc('user_1').update({
        currentCircleId: 'circle_non_existent'
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

    it('denies user from forging circle ownership to someone else on creation', async () => {
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
  // 5. MEMBER DISPLAY DOCUMENTS (NO PII) & ROLE INTEGRITY
  // ──────────────────────────────────────────────────────────
  describe('5. Member Display Documents (/circles/{id}/members/{id})', () => {
    before(async () => {
      const alice = testEnv.authenticatedContext('alice_s').firestore();
      await alice.collection('circles').doc('circle_sub').set({
        id: 'circle_sub',
        name: 'Sub Test Orbit',
        ownerId: 'alice_s',
        inviteCode: 'SO-SUB-TEST',
        memberIds: ['alice_s', 'bob_s']
      });
      await alice.collection('circles').doc('circle_sub').collection('members').doc('alice_s').set({
        id: 'alice_s',
        userId: 'alice_s',
        name: 'Alice S',
        role: 'Owner',
        avatarInitial: 'A',
        isSharingActive: false,
        joinedAt: 1000
      });
      await alice.collection('circles').doc('circle_sub').collection('members').doc('bob_s').set({
        id: 'bob_s',
        userId: 'bob_s',
        name: 'Bob S',
        role: 'Member',
        avatarInitial: 'B',
        isSharingActive: false,
        joinedAt: 1000
      });
    });

    it('allows verified circle members to read member display cards (verified NO PII)', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      const card = await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').get()
      );
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

    it('allows member to update their own permitted fields (name, avatar, consent toggle)', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertSucceeds(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            name: 'Bob S Updated',
            isSharingActive: true,
            statusText: 'Consent Given'
          })
      );
    });

    it('denies member from modifying protected fields on their own card (userId, id, role, joinedAt)', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertFails(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            role: 'Owner' // Escalation denied
          })
      );
      await assertFails(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            userId: 'alice_s' // Impersonation denied
          })
      );
    });

    it('denies member from modifying another members display card or role', async () => {
      const bob = testEnv.authenticatedContext('bob_s').firestore();
      await assertFails(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').update({
            name: 'Bob Modified Alice'
          })
      );
      await assertFails(
        bob.collection('circles').doc('circle_sub')
          .collection('members').doc('alice_s').update({
            role: 'Demoted'
          })
      );
    });

    it('allows circle owner to update member role or remove card', async () => {
      const alice = testEnv.authenticatedContext('alice_s').firestore();
      await assertSucceeds(
        alice.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').update({
            role: 'Moderator'
          })
      );
      await assertSucceeds(
        alice.collection('circles').doc('circle_sub')
          .collection('members').doc('bob_s').delete()
      );
    });
  });

  // ──────────────────────────────────────────────────────────
  // 6. INVITATION INTEGRITY & JOINING VALIDATION
  // ──────────────────────────────────────────────────────────
  describe('6. Invitation Integrity & Circle Joining Controls', () => {
    const inviteCode = 'SO-JOIN-999';
    const circleId = 'circle_join_test';

    before(async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await owner.collection('circles').doc(circleId).set({
        id: circleId,
        name: 'Joinable Circle',
        ownerId: 'owner_j',
        inviteCode: inviteCode,
        memberIds: ['owner_j']
      });
      // Valid active invite matching this circle
      await owner.collection('invites').doc(inviteCode).set({
        code: inviteCode,
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() + (24 * 60 * 60 * 1000)
      });

      // Unrelated circle and invite
      await owner.collection('circles').doc('circle_unrelated').set({
        id: 'circle_unrelated',
        name: 'Unrelated Circle',
        ownerId: 'owner_j',
        inviteCode: 'SO-UNRELATED-CODE',
        memberIds: ['owner_j']
      });
      await owner.collection('invites').doc('SO-UNRELATED-CODE').set({
        code: 'SO-UNRELATED-CODE',
        circleId: 'circle_unrelated',
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() + (24 * 60 * 60 * 1000)
      });
    });

    it('allows creator to create an invite valid up to 48 hours for a circle they belong to', async () => {
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

    it('denies non-owner member from creating an invite code for the circle', async () => {
      const member = testEnv.authenticatedContext('member_j').firestore();
      await assertFails(member.collection('invites').doc('SO-MEMBER-INV').set({
        code: 'SO-MEMBER-INV',
        circleId: circleId,
        createdBy: 'member_j',
        isActive: true,
        expiresAt: Date.now() + (24 * 60 * 60 * 1000)
      }));
    });

    it('allows circle owner to deactivate an invitation', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await assertSucceeds(owner.collection('invites').doc(inviteCode).update({
        isActive: false
      }));
      // Re-enable for subsequent join tests
      await assertSucceeds(owner.collection('invites').doc(inviteCode).update({
        isActive: true
      }));
    });

    it('denies non-owner from deactivating or modifying an invitation', async () => {
      const member = testEnv.authenticatedContext('member_j').firestore();
      await assertFails(member.collection('invites').doc(inviteCode).update({
        isActive: false
      }));

      const attacker = testEnv.authenticatedContext('attacker').firestore();
      await assertFails(attacker.collection('invites').doc(inviteCode).update({
        isActive: false
      }));
    });

    it('denies reading an expired invite', async () => {
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await owner.collection('invites').doc('SO-EXPIRED').set({
        code: 'SO-EXPIRED',
        circleId: circleId,
        createdBy: 'owner_j',
        isActive: true,
        expiresAt: Date.now() - (60 * 1000)
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
        isActive: false,
        expiresAt: Date.now() + (24 * 60 * 60 * 1000)
      });

      const member = testEnv.authenticatedContext('member_j').firestore();
      await assertFails(member.collection('invites').doc('SO-DEACTIVATED').get());
    });

    it('allows a member to join when a valid active invite matching this circle exists', async () => {
      const member = testEnv.authenticatedContext('member_j').firestore();

      // Step 1: Member creates their initial member card
      await assertSucceeds(
        member.collection('circles').doc(circleId)
          .collection('members').doc('member_j').set({
            id: 'member_j',
            userId: 'member_j',
            name: 'New Member',
            role: 'Member',
            avatarInitial: 'N',
            isSharingActive: false
          })
      );

      // Step 2: Member joins by adding self to memberIds
      await assertSucceeds(
        member.collection('circles').doc(circleId).update({
          memberIds: ['owner_j', 'member_j']
        })
      );
    });

    it('denies joining using an unrelated invitation code that belongs to a different circle', async () => {
      // Setup a circle that specifies an invite code pointing to circle_unrelated
      const owner = testEnv.authenticatedContext('owner_j').firestore();
      await owner.collection('circles').doc('circle_mismatch').set({
        id: 'circle_mismatch',
        name: 'Mismatch Circle',
        ownerId: 'owner_j',
        inviteCode: 'SO-UNRELATED-CODE',
        memberIds: ['owner_j']
      });

      const attacker = testEnv.authenticatedContext('attacker').firestore();
      await assertFails(
        attacker.collection('circles').doc('circle_mismatch').update({
          memberIds: ['owner_j', 'attacker']
        })
      );
    });

    it('denies joining when NO active invite exists', async () => {
      const owner = testEnv.authenticatedContext('owner_no_inv').firestore();
      const privateCircleId = 'circle_no_invite';

      await owner.collection('circles').doc(privateCircleId).set({
        id: privateCircleId,
        name: 'Private No Invite',
        ownerId: 'owner_no_inv',
        inviteCode: 'SO-NON-EXISTENT',
        memberIds: ['owner_no_inv']
      });

      const attacker = testEnv.authenticatedContext('attacker').firestore();
      await assertFails(
        attacker.collection('circles').doc(privateCircleId).update({
          memberIds: ['owner_no_inv', 'attacker']
        })
      );
    });
  });
});
