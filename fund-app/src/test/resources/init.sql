CREATE TABLE EXCHANGE_RATES (
        CURRENCY varchar(255) not null,
        LAST_UPDATED_AT date,
        RATES json,
        XRATE_LOCK_VERSION integer default 0
);

INSERT INTO EXCHANGE_RATES VALUES ('USD', '2025-09-01', null, 0);