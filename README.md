# Japter

**Android Application** filter notifications to connect custom API.

## Features

- Received notification data to send custom backend.
- Retry send data when error(response code is not 200) until success.
- Send data encoded is security.
- Can see notification histories.
- Can block for skip noise notifications.
- Can custom public key (Sealer)
- Can custom https enpoint
- Can see all stats (enqueued,failed,running,scceeded,others)
- Can cancel all queued.

## Working Flow

**Received notification data** -> **Filter with setting** -> **Encoded data by Sealer** -> **Send data to API** -> **Check response code** -> **Retry send data if error until success**
