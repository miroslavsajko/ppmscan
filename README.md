# PPMScan

PPMScan is a tool for scanning managers in Power Play manager. It can find managers that are not so active, but still have active teams.

## Usage

Double click on the .bat file. Requires Java 8 or higher.

## Configuration

Configuration is located in the file named PPMScanConfig.json.

### managerIdFrom and managerIdTo

Describes the range of manager ids that will be scanned. The range is inclusive.

### managerIds

You can specify also specific manager ids that will be scanned outside of your range.

### requestedSports

A list of sports in which a manager must have a team to be included in the result. If a manager doesn't have a team from requested sports, the manager is skipped. Allowed values are "HOCKEY", "SOCCER", "HANDBALL" and "BASKETBALL".

### lastLoginDaysRecentlyActiveThreshold

Threshold for how a manager is considered active. If he logged in less than X days ago, he is considered active and skipped.

### lastLoginDaysInactiveThreshold

Threshold for how a manager is considered inactive. If he logged in more than X days ago, he is considered inactive and skipped.

### lastLoginDayDifferenceSumThreshold

Threshold for calculating a manager's activity. For each login entry a difference between now and the login date is calculated and summed - if the sum is less than X, he is skipped.

### millisecondsBetweenPageLoads

Waiting time between page loads. It is meant to decrease the load on the server to not overload a server and to not cause a DDoS attack.

### sizeOfThreadPool

How many threads will load pages in parallel.

### chunkSize

How many managers are processed in one iteration, in other words, after how many managers a save of current state is triggered.

### exportFormat

Format in which found managers are exported. Accepted values are "JSON" and "EXCEL".

## IgnoredManagers

This file contains a list of managers (their ids) which are either blocked or they have never logged into their account. These managers are always skipped and their manager pages is never loaded again. After every run the list is updated with new entries. The list can be edited.

## Licence

[MIT](https://choosealicense.com/licenses/mit/)