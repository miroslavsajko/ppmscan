# PPMScan

PPMScan is a tool for scanning managers in Power Play manager. It can find managers that are not so active, but still have active teams.

## Configuration

Configuration is located in the file named PPMScanConfig.json.

### managerIdFrom and managerIdTo

Describes the range of manager ids that will be scanned. The range is inclusive.

### managerIds

You can specify also specific manager ids that will be scanned outside of your range.

### requestedSports

A list of sports in which a manager must have a team to be included in the result. If a manager doesn't have a team from requested sports, the manager is skipped. Allowed values are "HOCKEY", "SOCCER", "HANDBALL" and "BASKETBALL".

### lastLoginDaysRecentlyActiveThreshold

Threshold for how a manager is considered inactive. If his last login was less than X days ago, he is considered inactive and is included in the result.

### lastLoginDayDifferenceSumThreshold

Threshold for calculating a manager's activity. For each login entry a difference between now and the login date is calculated and summed - if the sum is more than X, he is considered inactive and he is included in the result. This is a good indicator for users that are active irregularly

Example: Today is 20th day of a month, the manager logged in on 19th, 16th, 11th, 9th and 5th. The differences is 1, 4, 9, 11 and 15, it's sum is 40.    

### lastLoginCriteriaMatch

There are 2 last login criteria, this setting defines if a manager must fulfill both criteria (configured value 2) or only 1 of them to be included in the result.

### millisecondsBetweenPageLoads

Waiting time between page loads. It is meant to decrease the load on the server to not overload a server and to not cause a DDoS attack.

### ignoreListLastLoginMonthsThreshold

Threshold for how many months the user must be inactive in order to be added to the ignore list.

### ignoredManagersFormat

Format which is used to import and export the ignored managers. Possible values are JSON and SQLITE.

### sizeOfThreadPool

How many threads will load pages in parallel.

### chunkSize

How many managers are processed in one iteration, in other words, after how many managers a save of current state is triggered.

### exportFormat

Format in which found managers are exported. Accepted values are "JSON", "EXCEL" and "HTML".

## IgnoredManagers

This file contains a list of managers (their ids) which are either blocked in PPM or they have never logged into their account. These managers are always skipped and their manager pages are never loaded again. After every run the list is updated with new entries. The list can be edited manually.

## License

[MIT](https://choosealicense.com/licenses/mit/)