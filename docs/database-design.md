# Database Design — AuthenticateSystem

## Tables

### users
| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK, default gen_random_uuid() | |
| email | VARCHAR(255) | NOT NULL, UNIQUE | |
| password | VARCHAR(255) | NULLABLE | null for OAuth2-only users |
| full_name | VARCHAR(100) | NOT NULL | |
| avatar_url | TEXT | NULLABLE | Cloudinary URL |
| provider | VARCHAR(20) | NOT NULL, default 'LOCAL' | LOCAL \| GOOGLE \| FACEBOOK |
| provider_id | VARCHAR(255) | NULLABLE | OAuth2 subject ID |
| enabled | BOOLEAN | NOT NULL, default true | |
| totp_enabled | BOOLEAN | NOT NULL, default false | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

### roles
| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | |
| name | VARCHAR(50) | NOT NULL, UNIQUE | USER \| ADMIN \| MODERATOR |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

### user_roles (join table)
| Column | Type | Constraints |
|---|---|---|
| user_id | UUID | FK → users.id ON DELETE CASCADE |
| role_id | UUID | FK → roles.id ON DELETE CASCADE |
| PRIMARY KEY | (user_id, role_id) | composite |

### refresh_tokens
| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | |
| user_id | UUID | NOT NULL, FK → users.id ON DELETE CASCADE | |
| token | VARCHAR(512) | NOT NULL, UNIQUE | hashed UUID |
| expires_at | TIMESTAMP | NOT NULL | |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

### totp_secrets
| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID | PK | |
| user_id | UUID | NOT NULL, UNIQUE, FK → users.id ON DELETE CASCADE | one per user |
| secret | VARCHAR(255) | NOT NULL | Base32 encoded, encrypted at rest |
| created_at | TIMESTAMP | NOT NULL | |
| updated_at | TIMESTAMP | NOT NULL | |

---

## What lives in Redis (not in PostgreSQL)

| Key pattern | Value | TTL | Purpose |
|---|---|---|---|
| `otp:email:{email}` | SHA-256(code) | 300s | Email OTP |
| `otp:whatsapp:{phone}` | SHA-256(code) | 300s | WhatsApp OTP |
| `otp:attempts:{target}` | integer counter | 300s | Failed OTP attempts |
| `otp:cooldown:{target}:{channel}` | "1" | 60s | Resend cooldown |
| `blacklist:token:{jti}` | "1" | remaining token TTL | Revoked JWT blacklist |
| `rate:limit:{action}:{identifier}` | Bucket4j state | managed | Rate limit buckets |

---

## Design Decisions

**Why UUID for primary keys?** Avoids sequential ID guessing in URLs. Works across distributed inserts without coordination.

**Why `password` is nullable?** A user who registers via Google never sets a password. Nullable makes this explicit rather than storing a dummy value.

**Why `totp_secrets` is a separate table?** Keeps `users` lean. Most users will never enable TOTP — a separate table avoids a nullable column on every row and makes the 1:0-1 relationship explicit in the schema.

**Why `refresh_tokens` has `expires_at` in the DB?** Allows querying and cleaning up expired tokens in a maintenance job, independent of Redis state.

**Why no `otp_records` table?** OTP is ephemeral by nature — TTL in Redis is the right tool. A DB table would require a cleanup job and adds write load on every OTP operation.