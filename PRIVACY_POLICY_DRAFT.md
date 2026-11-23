# SafeOrbit Privacy Policy (Draft)

**Last Updated:** November 23, 2026  
**Application Name:** SafeOrbit  
**Package Name:** `com.ogken.safeorbit`  
**Publisher/Developer:** [Project Owner / Developer Name Placeholder]  
**Contact:** [Developer Contact Email Placeholder]  

---

### 1. Introduction
SafeOrbit is a consent-first family safety and location-sharing application. Our core philosophy is **Zero Tracking by Default**. Your location is private until you explicitly choose to share it with authorized members of a family circle you have joined.

This Privacy Policy explains:
- What information SafeOrbit collects and processes.
- Why we collect it and how it is used.
- How you control, stop, and delete your data.
- The third-party services SafeOrbit interacts with.

---

### 2. Information We Collect

#### A. Account & Profile Information
- **Account Credentials:** When you register, we collect your email address and authentication credentials securely managed via Firebase Authentication. Passwords are never accessible to SafeOrbit in plaintext.
- **Display Profile:** Name, emergency contact phone number (optional), role/relationship label within your family circle (e.g., "Parent", "Child"), and an avatar initial.

#### B. Location Information (Voluntary & Consent-Based Only)
- **Geographic Coordinates:** Latitude, longitude, horizontal accuracy, and timestamp.
- **Battery Status:** Device battery percentage (transmitted alongside location updates to inform family members of impending power loss).
- **Collection Mode:** 
  - Location sharing is **OFF by default**.
  - No background location is collected without user initiation.
  - An explicit, informative **Location Consent Dialog** is displayed before any Android runtime location permission is requested.
  - While location sharing is active, Android displays an ongoing, persistent notification clearly indicating that SafeOrbit is sharing your live location.
  - Location access supports both **Precise** (fine GPS) and **Approximate** (coarse network-based) permissions.

#### C. Family Circle & Invitation Information
- **Circle Data:** Circle name, description, circle owner UID, and list of confirmed member UIDs.
- **Invitation Codes:** Temporary, cryptographically randomized invitation codes (valid for up to 48 hours) used to invite family members.

#### D. What We Do NOT Collect
- We **do not** collect contacts, address books, call logs, SMS messages, browsing history, audio/camera recordings, device identifiers (IMEI/MAC address), or biometric data.
- We **do not** sell, rent, monetize, or broker personal information to advertisers or data brokers.

---

### 3. How We Use Information
We use your information exclusively to provide the core safety features of SafeOrbit:
1. Authenticating your identity and maintaining your session.
2. Enabling you to create or join private family circles.
3. Broadcasting your real-time coordinates **only** to confirmed members of your active circle while you have explicitly enabled location sharing.
4. Displaying member markers on an OpenStreetMap base map via MapLibre Native.

---

### 4. Who Can See Your Information
- **Private Profiles:** Your email address and emergency contact phone number are restricted exclusively to your own authenticated account.
- **Member Display Records:** Only verified members who belong to your specific family circle can view your display name, role label, and avatar initial. Outsiders cannot read or query family circle data.
- **Location Records:** Live location coordinates are readable **only** by confirmed members of your active family circle while your sharing status is active. When you stop sharing or leave a circle, your location records are immediately marked inactive or permanently deleted.

---

### 5. Third-Party Services & Map Providers

SafeOrbit uses selected open-source and cloud infrastructure providers:
- **Firebase Authentication & Cloud Firestore (Google LLC):** Used for secure account authentication, database storage, and security rules enforcement.
- **OpenStreetMap (OpenStreetMap Foundation):** Used for rendering raster map tiles (`tile.openstreetmap.org`). Map tile requests send a compliant application User-Agent header (`SafeOrbit/<version> (Android; com.ogken.safeorbit)`) and use standard HTTP caching. No personal user data or GPS coordinates are ever transmitted to OpenStreetMap servers.
- **MapLibre Native Android:** An open-source, client-side rendering engine that renders map tiles without tracking user activity or requiring proprietary API keys or billing accounts.

---

### 6. Data Retention & Account Deletion

- **Location Data Retention:** SafeOrbit does not maintain historical location tracking trails. When location sharing is stopped, live coordinates are expired or deleted.
- **Leaving a Circle:** When you leave a circle (or are removed by the circle owner), your location document and member membership in that circle are immediately purged.
- **Permanent Account Deletion:** You have the absolute right to delete your account at any time directly within the application:
  - Navigate to **Settings -> Account Settings -> Delete Data**.
  - Confirming deletion immediately terminates all location broadcasts, deletes your location documents, removes you from all circles, erases your user profile in Cloud Firestore, and permanently deletes your Firebase Authentication account.

---

### 7. Security Safeguards
- All data transmission between SafeOrbit and backend infrastructure is encrypted using Transport Layer Security (TLS/HTTPS).
- Database access is strictly governed by verified server-side Cloud Firestore Security Rules validating document ownership, circle membership, coordinate boundaries, and invite expiration.

---

### 8. Changes to this Policy
If we make material changes to how location or personal data is handled, we will update this document and provide notification through the application.

---

*This draft policy is tailored to SafeOrbit's actual technical implementation. Before production public distribution (e.g., on Google Play), the project owner must replace all bracketed placeholders with legal entity details and verified support contact information.*
