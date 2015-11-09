package com.zanvork.hibernate;

import org.hibernate.dialect.MySQL5Dialect;

/**
 *
 * @author zanvork
 */
public class CaseSensitiveDialect extends MySQL5Dialect{
    @Override
    public String getTableTypeString() {
        return " COLLATE utf8_bin";
    }
}
