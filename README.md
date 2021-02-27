# League-of-League-Tracker

## DEMO

<img src='demo.gif' title='Demo video' width='' alt='Demo video' />

## 1. User Stories (Required and Optional)

**Required Must-have Stories**

 * [ ] User can search stats by summoner name
 * [ ] User can view recent match history 
 * [ ] User can see current ranking,graph of win/loss ratio, and average KDA
 * [ ] User can click/tap on a match for more info

**Optional Nice-to-have Stories**

 * [ ] Infinite pagination for match history - recent 10 can be continually expanded to more matches
 * [ ] Favorites/Recent searches listed on screen for convenience

## 2. Screen Archetypes

 * Summoner Search screen (Login)
   * Input bar for user to specify the summoner to get stats for
   * Favourites/Recent searches bar
 * Summoner Stats screen (Stream/Feed)
   * List of recent matches with small amount of important stats on view
   * Ranking (Tier and LP) details, win/loss pie chart, and average KDA for displayed matches listed near top of screen
   * Should be able to click on match to get more detail (implementation tbd) 
 * Match detail screen / Dropdown? (TBD) 

## 3. Navigation

**Tab Navigation** (Tab to Screen)

 * Summoner search screen
 * ...

**Flow Navigation** (Screen to Screen)

 * Login/Summoner Search screen
   * User inputs summoner name in search bar (or as much as they remember)
   * User clicks on summoner they want to view details/stats for from results
   * Stretch - user can use a box of 'favorite/starred' summoners for quick access
 * Feed/Summoner Stats screen
   * User can scroll up and down a list of recent match summaries
   * User can click on a match for more details (deciding whether to expand details on same screen or navigate to Detail/Match Detail screen tbd)

## 4. Wireframes
<img src='https://github.com/Breakout-Room-2/lol-tracker/blob/master/wireframes.jpg' title='Wireframes' width='' alt='Wireframes' />

## 5. Schema

### Models

#### Summoner
Name      | Type      | Description
--------- | :-------: | -----------
accountID | String    | Encrypted account ID
name      | Sring     | Summoner name
id        | String    | Encrypted summoner ID
    - Note: encrypted values are encrypted using api key, thus are unique per api key

#### Mastery
Name          | Type  | Description
------------- | :---: | -----------
championID    | int   | Champion ID
championLevel | int   | Mastery Level for a champion
championPoints| int   | Mastery Points for a champion

#### League
 Name           | Type          | Description
 -------------- | :-----------: | -----------
 queueType      | String        | SoloQueue, Flex
 tier           | String        | Iron, Bronze, Silver, Gold, etc.
 rank           | String        | rank I, II, III, IV within tier
 leaguePoints   | int           | lp progression towards next rank/tier
 wins           | int           | total ranked wins
 losses         | int           | total ranked losses
 hotStreak      | boolean       | 3+ wins in a row
 miniSeries     | MiniSeries    | series if summoner is in promos

#### MiniSeries
 Name       | Type      | Description
 ---------- | :-------: | -----------
 losses     | int       | number of losses
 progress   | int       | W,L,N representation of series
 target     | int       | required wins to promote
 win        | String    | number of wins
 
#### Matchlist
Name      | Type                      | Description
--------- | :-----------------------: | -----------
matches   | List [MatchReference]     | List of match references

#### MatchReference
Name      | Type      | Description
--------- | :-------: | -----------
gameID    | long      | game ID unique to match
role      | String    | role of user in match 
champion  | int       | (same as championID)
queue     | int       | type of queue (e.g. ranked, draft)
timestamp | long      | Date of match (in UnixEpochMilliseconds)

#### Match
Name          | Type                  | Description
------------- | :-------------------: | -----------
queueId       | int                   | type of queue and map
gameCreation  | long                  | (same as timestamp)
gameDuration  | long                  | match duration in seconds
participants  | list[particpant]      | list of match participants

#### participant
Name          | Type                  | Description
------------- | :-------------------: | -----------
particpantID  | int                   | participant ID
championID    | int                   | (same as championID)
teamId        | int                   | 100 for blue, 200 for red side
spell1Id      | int                   | first summoner spell ID
spell2Id      | int                   | second summoner spell ID
stats         | participantStats      | 

#### participantStats
Name                | Type                  | Description
-------------       | :-------------------: | -----------
item[0-6]           | int                   | itemID (resolved to item name/icon with db)
champLevel          | int                   | champion level
kills               | int                   | champion kills
deaths              | int                   | champion deaths
assists             | int                   | champion assists
totalDamageDealt    | long                  | total damage dealt
totalMinionsKilled  | int                   | CS score
visionScore         | int                   | vision score (calculated by riot)
visionWards         | int                   | control wards bought
wardsPlaced         | int                   | total wards placed
wardsKilled         | int                   | total wards killed
goldEarned          | int                   | gold earned
perk[0-5]           | int                   | runes used by ID (0-3 primary, 4-5 secondary)
perkPrimaryStyle    | int                   | rune path for primary rune
perkSubStyle        | int                   | rune path for secondary rune
statPerk[0-2]       | int                   | rune shards chosen (offense, flex, defense)

### Networking

#### List of network requests by screen
- Home/Search Screen
  - GET: request summoner info by name after user inputs name
- Feed Screen
  - GET: request champion mastery info to list top 3 (if not already cached) 
  - GET: request league info to display ranking(s)
  - GET: request match list info
  - GET: subsequent requests for match details on first 5 matches (for matches not already cached)

### Existing API Endpoints
Base Url: [https://na1.api.riotgames.com](https://na1.api.riotgames.com)
HTTP Verb   | Endpoint  | Description
----------- | --------- | -----------
'GET'       | /lol/summoner/v4/summoners/by-name/{summonerName} | Returns summoner IDs. Needed for other calls
'GET'       | /lol/match/v4/matchlists/by-account/{encryptedAccountID} | Returns brief summary of 100 matches
'GET'       | /lol/match/v4/matches/{matchID}   | Returns details of a particular match
'GET'       | /lol/champion-mastery/v4/champion-masteries/by-summoner/{encryptedSummonerID} | Return champion mastery data
'GET'       | /lol/league/v4/entries/by-summoner/{encryptedAccountID}   | Returns JSONList of league info (ranking)
