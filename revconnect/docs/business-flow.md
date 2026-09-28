# RevConnect – Business Flow

## Purpose

This document defines the **business workflow of RevConnect** and the responsibilities of each module during the **monolith phase**.

The goal is to make it clear to every developer:

* What the user can do.
* What happens after each action.
* Which module is responsible for the action.
* How modules interact with each other.
* Where business logic should be implemented.

This document does **not** define detailed API endpoints, database columns, or microservice architecture.

---

# 1. User Registration & Login

```text
User
 ↓
Register / Login
 ↓
Choose Account Type
(Personal / Creator / Business)
 ↓
Create / Manage Profile
 ↓
Home
```

### Workflow

**Registration**

1. User provides registration details.
2. User selects an account type:

    * Personal
    * Creator
    * Business
3. The system validates the registration data.
4. User information is stored.
5. The user profile is created.
6. User can log in.

**Login**

1. User provides login credentials.
2. System validates the credentials.
3. System identifies the user's account type and role.
4. User is allowed to access the application.
5. User is redirected to Home.

### Workflow Implementation

**Responsible Module:** `User`

```text
Frontend
   ↓
UserController
   ↓
UserService
   ↓
UserRepository
   ↓
MySQL
```

* `UserController` handles registration, login and profile-related requests.
* `UserService` contains user-related business logic.
* `UserRepository` handles database operations.
* User authentication and role-related checks belong to the User module.
* Other modules should use the user information when they need to identify the current user.

---

# 2. Profile Management

```text
User
 ↓
Profile
 ↓
View / Create / Edit Profile
 ↓
Save Changes
```

### Workflow

1. User opens their profile.
2. System retrieves the user's profile information.
3. User can create or update profile information.
4. Changes are validated.
5. Updated information is stored.
6. Updated profile is displayed.

### Workflow Implementation

**Responsible Module:** `User`

```text
UserController
      ↓
UserService
      ↓
UserRepository
      ↓
MySQL
```

The User module owns profile information.

Other modules should not directly modify user profile data.

---

# 3. Home & Feed

```text
User
 ↓
Home
 ↓
Personalized Feed
 ↓
View Posts
 ↓
Like / Comment / Share / Repost
```

### Workflow

1. User logs in and opens Home.
2. System requests posts that are relevant to the user.
3. Feed is generated using available user/network/content information.
4. Posts are displayed to the user.
5. User can interact with the displayed posts.
6. Interactions are handled by the Interaction module.

### Workflow Implementation

**Responsible Module:** `Feed`

```text
Frontend
   ↓
FeedController
   ↓
FeedService
   ↓
Required Repositories / Modules
   ↓
MySQL
```

* `FeedController` handles feed requests.
* `FeedService` contains feed-generation logic.
* Feed is responsible for deciding **which posts should be shown**.
* Feed does not own post creation or post interaction logic.
* Post information belongs to the Post module.
* Like/comment/share/repost operations belong to the Interaction module.

---

# 4. Create & Manage Posts

```text
User
 ↓
Create Post
 ↓
Content + Hashtags
 ↓
Optional:
CTA / Product / Schedule / Pin
 ↓
Publish
 ↓
Post Appears in Feed
```

### Workflow

**Create Post**

1. User chooses to create a post.
2. User enters post content.
3. User can add hashtags.
4. Creator/Business users can use the applicable content options.
5. User publishes the post.
6. System validates the post.
7. Post is stored.
8. Published post becomes available for the feed.

**Manage Post**

The post owner can manage their own post according to the supported operations.

```text
User
 ↓
Create / Edit / Delete Post
 ↓
Post Module
 ↓
Database
```

### Workflow Implementation

**Responsible Module:** `Post`

```text
Frontend
   ↓
PostController
   ↓
PostService
   ↓
PostRepository
   ↓
MySQL
```

* `PostController` handles post-related requests.
* `PostService` contains post business logic.
* `PostRepository` handles post persistence.
* Post ownership and post-related validation are handled by the Post module.
* The Post module owns post data.
* The Feed module reads published posts when generating the feed.
* The Interaction module handles likes, comments, shares and reposts.

---

# 5. Connections & Following

```text
User
 ↓
Search / Profile
 ↓
Connect / Follow
 ↓
Connection Request
 ↓
Accept / Reject
 ↓
Followers / Following
 ↓
Feed Updates
```

### Workflow

**Connection**

1. User finds another user.
2. User sends a connection request.
3. System stores the pending request.
4. Target user receives a notification.
5. Target user accepts or rejects the request.
6. If accepted, the connection becomes active.
7. The users become part of each other's network.

**Follow**

1. User selects another user to follow.
2. System records the follow relationship.
3. Following information is updated.
4. The relationship can be used when generating the feed.

**Unfollow**

1. User chooses to unfollow.
2. System removes/deactivates the follow relationship.
3. Following information is updated.

### Workflow Implementation

**Responsible Module:** `Connection`

```text
Frontend
   ↓
ConnectionController
   ↓
ConnectionService
   ↓
ConnectionRepository
   ↓
MySQL
```

* `ConnectionController` handles connection and follow requests.
* `ConnectionService` contains connection/follow business logic.
* `ConnectionRepository` manages connection-related database operations.
* Connection data belongs to the Connection module.
* The Feed module can use connection/follow information.
* The Notification module can be triggered when a connection-related action occurs.

---

# 6. Search

```text
User
 ↓
Search
 ↓
People / Posts / Hashtags / Creators / Businesses
 ↓
View Profile / Content
 ↓
Connect / Follow / Interact
```

### Workflow

1. User enters a search query.
2. System processes the query.
3. Matching users/content are retrieved.
4. Search results are displayed.
5. User can open a profile or post.
6. User can then perform supported actions such as:

    * Connect
    * Follow
    * View content
    * Interact with content

### Workflow Implementation

Search may require information from more than one module.

```text
Frontend
   ↓
Search Request
   ↓
Relevant Module
   ↓
Service
   ↓
Repository
   ↓
MySQL
```

The important responsibility is:

* User-related search → User module
* Post/content-related search → Post module
* Connection/follow actions → Connection module
* Content interactions → Interaction module

Search should **not duplicate the business logic** of these modules.

---

# 7. Post Interactions

```text
User
 ↓
View Post
 ↓
Like / Comment / Share / Repost
 ↓
Interaction Stored
 ↓
Post Engagement Updated
 ↓
Notification Generated
```

### Workflow

**Like**

```text
User
 ↓
Like Post
 ↓
Validate User + Post
 ↓
Store Like
 ↓
Update Engagement
 ↓
Notification
```

**Comment**

```text
User
 ↓
Add Comment
 ↓
Validate Comment
 ↓
Store Comment
 ↓
Update Engagement
 ↓
Notification
```

**Share / Repost**

```text
User
 ↓
Share / Repost
 ↓
Validate Action
 ↓
Store Interaction
 ↓
Update Engagement
 ↓
Notification
```

### Workflow Implementation

**Responsible Module:** `Interaction`

```text
Frontend
   ↓
InteractionController
   ↓
InteractionService
   ↓
InteractionRepository
   ↓
MySQL
```

* `InteractionController` handles interaction requests.
* `InteractionService` contains interaction business logic.
* `InteractionRepository` stores interaction data.
* Interaction module owns likes, comments, shares and reposts.
* The Post module owns the actual post.
* The Notification module handles notifications generated from interactions.

---

# 8. Notifications

```text
User Activity
 ↓
Like / Comment / Follow / Connect / Repost
 ↓
Notification
 ↓
Notification Stored
 ↓
User Views Notification
```

### Workflow

1. A user performs an action.
2. The relevant module processes the action.
3. If the action requires a notification, notification information is created.
4. Notification is handled by the Notification module.
5. Notification is stored.
6. The target user can view it.

### Workflow Implementation

**Responsible Module:** `Notification`

```text
Other Module
     ↓
NotificationService
     ↓
NotificationRepository
     ↓
MySQL
```

Examples:

```text
Like
 ↓
Interaction Module
 ↓
Notification Module
 ↓
Post Owner receives notification
```

```text
Follow
 ↓
Connection Module
 ↓
Notification Module
 ↓
Target User receives notification
```

The Notification module owns notification data and notification-related operations.

---

# 9. Creator & Business

```text
Creator / Business
 ↓
Enhanced Profile
 ↓
Create Content
 ↓
CTA / Products / Promotional Content / Scheduling
 ↓
Publish
 ↓
Audience Engagement
 ↓
Analytics
```

### Workflow

Creator and Business accounts use the common RevConnect workflow with additional supported capabilities.

```text
Creator / Business
       ↓
     Profile
       ↓
     Content
       ↓
     Publish
       ↓
   Audience
       ↓
 Engagement
       ↓
  Analytics
```

### Workflow Implementation

* Account type is managed by the User module.
* Profile information is managed by the User module.
* Content creation is handled by the Post module.
* CTA/product/promotional post information is handled as part of post functionality.
* Scheduling and pinning are handled by the Post module.
* Audience interactions are handled by the Interaction module.
* Engagement information is used for analytics.

No separate Creator or Business module is required during the monolith phase.

---

# 10. Analytics

```text
Post
 ↓
Audience Views / Interactions
 ↓
Engagement Data
 ↓
Analytics
 ↓
Creator / Business
```

### Workflow

1. Creator or Business publishes content.
2. Users interact with the content.
3. Interaction information is stored.
4. Engagement information can be used to generate analytics.
5. Creator/Business user can view the available analytics.

### Workflow Implementation

Analytics uses information produced by the Post and Interaction modules.

```text
Post Module
      │
      ├──────────┐
      │          │
      ▼          ▼
Interaction   Engagement
      │          │
      └────┬─────┘
           ▼
       Analytics
```

Analytics should use existing post and interaction information instead of maintaining duplicate copies of the same business data.

---

# 11. Complete Business Workflow

```text
                         USER
                           │
                           ▼
                   Register / Login
                           │
                           ▼
                    Profile Setup
                           │
                           ▼
                         HOME
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
           FEED          SEARCH        PROFILE
             │             │             │
             │             │        Connect / Follow
             │             │             │
             ▼             ▼             ▼
           POSTS      Users / Content   Network
             │
      ┌──────┼──────────┐
      ▼      ▼          ▼
    Like   Comment    Share
      │      │          │
      └──────┼──────────┘
             ▼
          Repost
             │
             ▼
       Notifications

             │
             ▼
      Creator / Business
             │
             ▼
      Content + Products
             │
             ▼
         Analytics
```

---

# 12. Monolith Implementation

RevConnect is currently implemented as **one Spring Boot application**.

```text
Frontend
       │
       │ HTTP / REST
       ▼
┌───────────────────────────────────────┐
│         Spring Boot Monolith          │
│                                       │
│  ┌───────────┐   ┌───────────────┐   │
│  │   User    │   │     Post      │   │
│  └───────────┘   └───────────────┘   │
│                                       │
│  ┌───────────┐   ┌───────────────┐   │
│  │Connection │   │  Interaction  │   │
│  └───────────┘   └───────────────┘   │
│                                       │
│  ┌───────────┐   ┌───────────────┐   │
│  │   Feed    │   │ Notification  │   │
│  └───────────┘   └───────────────┘   │
│                                       │
└───────────────────┬───────────────────┘
                    │
                    ▼
                  MySQL
```

All modules are part of the **same Spring Boot application**.

There are no separate deployments for these modules during the monolith phase.

---

# 13. Module Responsibility

| Module           | Responsible For                                                                    |
| ---------------- | ---------------------------------------------------------------------------------- |
| **User**         | Registration, login, roles, profile, privacy                                       |
| **Post**         | Creating, editing and deleting posts, hashtags, CTA, products, scheduling, pinning |
| **Connection**   | Connections, requests, accept/reject, follow/unfollow, followers/following         |
| **Interaction**  | Likes, comments, shares, reposts, engagement                                       |
| **Feed**         | Personalized feed and feed-related post retrieval                                  |
| **Notification** | Notifications generated by user activities                                         |

### Important Rule

Each module should own its **own business logic**.

For example:

```text
PostController
      ↓
PostService
      ↓
PostRepository
```

Do not put Post business logic inside `FeedService`.

Similarly:

```text
ConnectionController
      ↓
ConnectionService
      ↓
ConnectionRepository
```

Do not put connection business logic inside `UserService`.

This keeps the monolith organized and makes it easier to separate these modules into microservices later.

---

# 14. Standard Module Structure

Each module should follow the same basic structure:

```text
module/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

Example:

```text
post/
├── controller/
│   └── PostController
├── service/
│   └── PostService
├── repository/
│   └── PostRepository
├── entity/
│   └── Post
└── dto/
    ├── PostRequest
    └── PostResponse
```

The same structure should be followed for:

```text
user/
post/
connection/
interaction/
feed/
notification/
```

---

# 15. Module Interaction Rules

Modules can communicate with each other when required because they are inside the same monolith.

Example:

```text
User
  ↓
Connection
  ↓
Feed
```

A user's connections can influence the posts retrieved by the Feed module.

Another example:

```text
User
  ↓
Post
  ↓
Interaction
  ↓
Notification
```

A user creates a post, another user interacts with it, and the post owner receives a notification.

The important rule is:

> **The module responsible for a business operation should own that operation.**

Other modules can use the required information, but should not duplicate or take ownership of that business logic.

---

