1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
# Project Estimation  
Authors: Teresa Torresani, Riccardo Ossella, Aurora Anna Pia Sergio, Antonio Macaluso

Date: 30/04/21

Version: 1.0
# Contents
- [Estimate by product decomposition]
- [Estimate by activity decomposition]
# Estimation approach
In this project we used a waterfall approach using a product and activity decomposition. After an estimation of our requirements we defined an iteration of a Gantt in preparation of the development phase. 

# Estimate by product decomposition
### 
|             | Estimate                        |             
| ----------- | ------------------------------- |  
| NC =  Estimated number of classes to be developed   |               25              |             
|  A = Estimated average size per class, in LOC       |                100            | 
| S = Estimated size of project, in LOC (= NC * A) |    2500 |
| E = Estimated effort, in person hours (here use productivity 10 LOC per person hour)  |     250                                 |   
| C = Estimated cost, in euro (here use 1 person hour cost = 30 euro) | 7500 | 
| Estimated calendar time, in calendar weeks (Assume team of 4 people, 8 hours per day, 5 days per week ) |         1,6           |               
# Estimate by activity decomposition
### 
|         Activity name    | Estimated effort (person hours)   |             
| ----------- | ------------------------------- | 
|Requirement | 32 |
|GUI prototype | 8 |
|Design | 44 |
|Coding | 250 |
|Test plan | 16 |
|Unit test | 171 |
|integration test | 30 |
|System test | 30 |
|Management | 30 |
###
Insert here Gantt chart with above activities

```plantuml
[Requirement] lasts 3 days
[GUI prototype] lasts 1 day
[Design] lasts 3 days
[Coding] lasts 9 days
[Test plan] lasts 2 days
[Unit test] lasts 6 days
[Integration test] lasts 2 days
[System test] lasts 2 days

Project starts 2021-04-22
Sunday are closed
Saturday are closed
[Requirement] starts 2021-04-22
[GUI prototype] starts at [Requirement]'s end
[Design] starts at [GUI prototype]'s end
[Coding] starts at [Design]'s end
[Test plan] starts at [Coding]'s end
[Unit test] starts at [Test plan]'s end
[Integration test] starts at [Unit test]'s end

[System test] starts at [Integration test]'s end
[Management] starts 2021-04-22
[Management] ends 2021-06-01

```
