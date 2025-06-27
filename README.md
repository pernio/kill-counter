# Kill counter
Paper plugin to track unique kills on weapons for extra lore.

### Features <br/>
| What?            | How?          | Description                                                          |
|------------------|---------------|----------------------------------------------------------------------|
| Track kills      | Kill a player | Adds an UUID from a player to a local JSON file.                     |
| Check last kill  | `/lastkilled` | Returns the name of the player who was killed last with the weapon.  |
| Delete kill data | `/resetkills` | Removes the data of the weapon you're holding.                       |
| Show an item     | `/showitem`   | Shows a hoverable item to the chat.                                  |

### Permissions <br/>
| Perm                     | Feature       |
|--------------------------|---------------|
| `killcounter.lastkilled` | `/lastkilled` |
| `killcounter.resetkills` | `/resetkills` |
| `killcounter.showitem`   | `/showitem`   |


### Configuration <br/>
| Perm                      | Default | Description                                          |
|---------------------------|---------|------------------------------------------------------|
| `cooldown`                | 60      | Cooldown to execute the show item command.           |