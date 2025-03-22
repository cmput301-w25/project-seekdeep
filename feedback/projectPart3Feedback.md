## Project Part 3 Feedback

### Code Base

All user stories for part 3 were completed. Code could be better organized using folders.

### Documentation

The docs are good, no issues really.

### Test Cases

Almost zero unit tests are present. You should add unit tests for things like data validation, other methods which have important logic in them, and instramented UI tests for user stories. Please do not make unit tests for getters and setters unless they have non-trvial logic in them.

### Object Oriented Design

Diagram could be layed out a bit clearer. Some cardinalities are incorrect, for exmaple a userProfile might have zero moods associated with it. A user never has to make a mood. The MainActicity does not need a create mood event fragment. This should be a 0..1 relationship. Many cardinalities have issues like this.

### Backlog

Backlog is clear and up to date.

### Sprint Planning and Review

Sprint planning and notes look good, but make sure everyone updates what they were working on, and if it's nothing I need justification.

### Demo 

Demo was smooth and all features worked.

### Tool Use

Make sure to assign yourself to PRs and make sure I can tell what a PR is about from the description; otherwise looks good to me.

### General Feedback

Good work overall, you just need a bit more organization of your code and processes for the next milestone. 
