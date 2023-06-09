package com.blog.task.domain.searchKeywordLog

import com.blog.task.domain.searchKeyword.Keyword
import java.time.ZonedDateTime

class SearchKeywordLog(
        val id : Long = 0,
        keyword: String,
        val createdAt :ZonedDateTime = ZonedDateTime.now()
){
    val keyword: Keyword = Keyword(keyword)

}

