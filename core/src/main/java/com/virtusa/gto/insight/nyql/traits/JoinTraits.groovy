package com.virtusa.gto.insight.nyql.traits

import com.virtusa.gto.insight.nyql.Join
import com.virtusa.gto.insight.nyql.Table

/**
 * @author IWEERARATHNA
 */
trait JoinTraits {

    /**
     * This is cross join.
     *
     * @param t other table to be joined with
     * @return new join instance
     */
    def JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx)
    }

    /**
     * Inner join
     *
     * @param t other table to be joined with.
     * @return new join instance
     */
    def INNER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "INNER_JOIN")
    }

    def LEFT_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "LEFT_OUTER_JOIN")
    }

    def RIGHT_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "RIGHT_OUTER_JOIN")
    }

    def RIGHT_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "RIGHT_JOIN")
    }

    def LEFT_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "LEFT_JOIN")
    }

    def FULL_OUTER_JOIN(Table t) {
        return new Join(table1: this, table2: t, _ctx: _ctx, type: "FULL_OUTER_JOIN")
    }

}