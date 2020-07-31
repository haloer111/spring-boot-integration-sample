    SELECT
        i.id as itemId,
        i.item_name as itemName,
        i.sell_counts as sellCounts,
        ii.url as imgUrl,
        tempSpec.price_discount as price,
				i.updated_time as updated_time
    FROM
        items i
    LEFT JOIN
        items_img ii
    on
        i.id = ii.item_id
    LEFT JOIN
        (SELECT item_id,MIN(price_discount) as price_discount from items_spec GROUP BY item_id) tempSpec
    on
        i.id = tempSpec.item_id
    WHERE
        ii.is_main = 1
    and i.updated_time > :sql_last_value
