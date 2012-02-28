
-- START CHANGE SCRIPT #1: 001_change.sql

-- contents of change script 1

INSERT INTO changelog (change_number, complete_dt, applied_by, description, checksum)
 VALUES (1, CURRENT_TIMESTAMP, USER(), '001_change.sql', 'c54d57ec2362c303a0e44718b120fb64b8eb519c53e5aac71b0272951f3bea6b');

COMMIT;

-- END CHANGE SCRIPT #1: 001_change.sql


-- START CHANGE SCRIPT #2: 002_change.sql

-- contents of change script 2

INSERT INTO changelog (change_number, complete_dt, applied_by, description, checksum)
 VALUES (2, CURRENT_TIMESTAMP, USER(), '002_change.sql', '470516b8894d7ada6f35fddbb085114b79da465bd2e171b25440ef1e7915c7d2');

COMMIT;

-- END CHANGE SCRIPT #2: 002_change.sql

