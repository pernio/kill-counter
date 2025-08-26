# Kill Tracker
Paper plugin to track unique kills on weapons for extra lore.

### Commands<br/>
| What                                                           | Perm                 | Description                                              |
|----------------------------------------------------------------|----------------------|----------------------------------------------------------|
| `/kt info`                                                     | `killtracker.use`    | Lists info about the current weapon you're holding.      |
| `/kt last`                                                     | `killtracker.use`    | Shows the name of the player that was killed last time.  |
| `/kt show`                                                     | `killtracker.show`   | Sends a message in the chat with a displayable overview. |
| `/kt reset`                                                    | `killtracker.reset`  | Resets the data from the current weapon you're holding.  |
| `/kt add {amount}`                                             | `killtracker.change` | Adds kills to a weapon.                                  |
| `/kt sub {amount}`                                             | `killtracker.change` | Subtracts kills to a weapon.                             |
| `/kt reload`                                                   | `killtracker.reload` | Reloads the config file.                                 |
| `/kt set {amount/streak/killed/killer} {amount/name} [#force]` | `killtracker.set`    | Sets a specific value to a weapon.                       |

### Permissions<br/>
| Perm                 | Child of            | Description                              |
|----------------------|---------------------|------------------------------------------|
| `killtracker.use`    |                     | Parent of default perms for all players. |
| `killtracker.admin`  |                     | Parent of perms for admins.              |
| `killtracker.show`   | `killtracker.use`   | Show an item in the chat.                |
| `killtracker.reset`  | `killtracker.use`   | Reset data on a weapon.                  |
| `killtracker.change` | `killtracker.admin` | Adds/subtracts kills to a weapon.        |
| `killtracker.reload` | `killtracker.admin` | Reloads the config file.                 |
| `killtracker.set`    | `killtracker.admin` | Sets a specific value to a weapon.       |