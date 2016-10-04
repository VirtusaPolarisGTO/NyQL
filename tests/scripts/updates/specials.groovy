/**
 * @author IWEERARATHNA
 */
[
        $DSL.update {
            TARGET (Address.alias("ad"))
            SET {
                EQ (ad.address2, IFNULL(ad.address2, STR("")))
            }
        },
        "UPDATE `Address` ad SET ad.address2 = IFNULL(ad.address2, \"\")",

]