# API reference

Base URL in local development: `http://localhost:8080`. Routes below are the literal controller mappings; there is no `/api` prefix in the current configuration. All endpoints use JSON unless shown as query parameters.

## Authentication

Routes outside `/user/auth/**` require an `Authorization` header using the bearer scheme with a signed access JWT. The current access-token lifetime is 24 hours; refresh-token lifetime is 180 days.

Security configuration permits all `/user/auth/**` requests. Signup, login, OTP, and password-reset initiation/confirmation are intended to be unauthenticated. Logout reads the current user from the security context, so include a valid bearer token for it to succeed. Other controller routes require authentication.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/user/auth/signup` | Create an unverified local account and send signup OTP. Optional `X-Device-Id` header. |
| `POST` | `/user/auth/signupOtp-verification` | Verify signup OTP, create profile, and return tokens. Optional `X-Device-Id`. |
| `POST` | `/user/auth/resend-otp` | Request another signup OTP. Optional `X-Device-Id`. |
| `POST` | `/user/auth/login` | Authenticate by email/password and return tokens. Optional `X-Device-Id`. |
| `POST` | `/user/auth/refresh/token` | Rotate tokens; send the refresh JWT in the `Authorization` header using the bearer scheme. |
| `PATCH` | `/user/auth/forgot-password` | Start password-reset email flow. |
| `PATCH` | `/user/auth/reset-otp-verify` | Validate reset OTP and receive a reset key. |
| `PATCH` | `/user/auth/change-password` | Set a new password using the reset key. |
| `POST` | `/user/auth/logout?allDevices=false` | Revoke the current device session; set `allDevices=true` to mark all of the user's token rows revoked. Optional `X-Device-Id` defaults to `DEFAULT_DEVICE`; an optional `FcmTokenRequest` body is accepted, but FCM token cleanup is not implemented. |

Signup request example:

```json
{
  "username": "Jordan Lee",
  "phone": "5551234567",
  "email": "jordan@example.com",
  "password": "correct-horse-9"
}
```

`username` is required and 5–50 characters; `phone` is required and exactly 10 digits; `email` is required and pattern-checked; `password` is required and at least 8 characters. The OTP verification body is `{"email":"jordan@example.com","otp":"123456"}`. Login uses `{"email":"jordan@example.com","password":"correct-horse-9"}`.

## Jobs and applications

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/user/job/createJob` | Create a job and active listing for the authenticated user. |
| `DELETE` | `/user/job/deleteJob/{jobId}` | Soft-delete a job owned by the current user and remove it from active listings. |
| `PATCH` | `/user/job/job-completed/{jobId}` | Mark an owned job complete and remove it from active listings. |
| `GET` | `/user/job/filter/getActiveJobs/nearby` | Search open jobs; query parameters are `lat`, `lng`, optional `radius`, `search`, `category`, `salary`, `page`, and `size`. |
| `GET` | `/user/job/filter/getMyJobs` | Return the current user's jobs; `page` defaults to `0`, `size` to `20`. |
| `POST` | `/user/job/applications/applyJob/{jobId}` | Apply to an open job (not the user's own job). |
| `GET` | `/user/job/applications/viewApplicants/{jobId}` | Owner-only applicant page; `page` defaults to `0`, `size` to `20`. |
| `POST` | `/user/job/applications/acceptApplicant/{jobId}/{applicantId}` | Owner accepts an application and moves the job to `IN_PROGRESS`. |
| `POST` | `/user/job/applications/rejectApplicant/{jobId}/{applicantId}` | Owner rejects an application while the job is open. |

Create-job request example:

```json
{
  "title": "Garden cleanup",
  "description": "Clear leaves from a small yard.",
  "category": "Home services",
  "payAmount": 75.00,
  "jobType": "FIXED",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "address": "1 Market Street",
  "jobTime": "2026-11-01T09:00:00Z"
}
```

Required fields are `title`, `description`, `category`, `payAmount`, `jobType`, `latitude`, `longitude`, `address`, and `jobTime`. Enum values for `jobType` are `HOURLY` and `FIXED`; service logic additionally requires positive pay and valid coordinates. Nearby search requires `lat` and `lng`; default radius is 50 km. Page size is limited to 10–100 by the search service. Salary is a minimum-pay threshold.

## Profiles

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/user/profiles/me` | Get the authenticated user's profile. |
| `GET` | `/user/profiles/public-profile/{jobId}` | Get the profile associated with a job's owner. Despite the route name, Spring Security currently requires authentication. |
| `PUT` | `/user/profiles/update` | Update non-null username, phone, headline, date of birth, skills, city, and description. |
| `PATCH` | `/user/profiles/update-address` | Update city and coordinates. |

Update-address example: `{"cityName":"San Francisco","latitude":37.7749,"longitude":-122.4194}`. Profile-edit fields are optional and are applied when non-null; the request DTO does not declare field-level validation.

## Responses and errors

Login and OTP-verification success responses include `timestamp`, `status`, `errorCode`, `message`, `path`, and `data`. The login `data` map contains `jwtAccessToken` and `jwtRefreshToken`. Paging responses use the same envelope with `errorMessage` and `data`; paged data contains `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, and `last`.

Application errors have this shape:

```json
{
  "timestamp": "2026-10-22T07:00:00Z",
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "errorMessage": "Request details are invalid",
  "path": "/user/job/createJob"
}
```

Validation failures add `validationErrors`, a field-to-message map. Malformed JSON and unknown request properties return `400`; unexpected exceptions are mapped to `500`. Custom exception status mappings include invalid request `400`, not found `404`, forbidden `403`, unauthorized `401`, and duplicate resource `409`. Some controllers return a status embedded in their success DTO, so clients should use the HTTP status as well as the body.

Social login and review-submission routes are not active in the current code.
