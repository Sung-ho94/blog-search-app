package com.blog.task.externalApi

import com.fasterxml.jackson.annotation.JsonFormat
import com.blog.task.domain.blogSearch.BlogSearch
import com.blog.task.domain.blogSearch.BlogSearchResponse
import com.blog.task.domain.blogSearch.SearchRequest
import kotlinx.serialization.Serializable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

private const val NAVER_API_HOST = "openapi.naver.com"
private const val NAVER_BLOG_SEARCH_API_PATH = "/v1/search/blog"

@Component
class NaverBlogSearch(
    @Qualifier("naverClientId") val naverClientId:String,
    @Qualifier("naverClientSecret") val naverClientSecret:String,
): BlogSearch {
    override fun getBlogData(searchRequest: SearchRequest): BlogSearchResponse? {
        val naverSearchRequest = searchRequest.toNaverSearchRequest()
        val webClient = WebClient.create()
//TODO: 2023/03/22 목 테스트 진행  - 성호
        return webClient.get()
                .uri{
                    it.scheme("https")
                            .host(NAVER_API_HOST)
                            .path(NAVER_BLOG_SEARCH_API_PATH)
                            .queryParam("query",naverSearchRequest.query)
                            .queryParam("display",naverSearchRequest.display)
                            .queryParam("start",naverSearchRequest.start)
                            .queryParam("sort",naverSearchRequest.sort.value)
                            .build()
                }
                .headers {
                    it.set("X-Naver-Client-Id", naverClientId)
                    it.set("X-Naver-Client-Secret", naverClientSecret)
                    it.contentType = MediaType.APPLICATION_JSON
                }.retrieve()
                .bodyToMono(NaverBlogSearchResponse::class.java)
                .block()?.toBlogSearchResponse()
    }

    private fun SearchRequest.toNaverSearchRequest(): NaverBlogSearchRequest {
        return NaverBlogSearchRequest(query, size, (page*size)-(size)+1, NaverBlogSearchRequest.Sort.of(this.sort.name))
    }
}

@Serializable
class NaverBlogSearchRequest(
        val query:String,
        val display:Int,
        val start:Int,
        val sort: Sort,
){
    enum class Sort(val value:String,val matchValue:String){
        SIM("sim","accuracy"),DATE("date","recency");
        companion object{
            fun of(value: String): Sort {
                Sort.values().forEach {
                    if(it.matchValue == value){
                        return it
                    }
                }
                throw ClassNotFoundException("NaverBlogSearchRequest sort Enum을 찾을 수 없습니다")
            }
        }
    }
}

class NaverBlogSearchResponse(
        val lastBuildDate:String,
        val total:Int,
        val start:Int,
        val display:Int,
        val items:List<Item>
){
    class Item(
            val title:String,
            val link: String,
            val description:String,
            val bloggername:String,
            val bloggerlink:String,
            @JsonFormat(pattern = "yyyyMMdd")
            val postdate: LocalDate,
    )

    fun toBlogSearchResponse(): BlogSearchResponse {
        return BlogSearchResponse(
                BlogSearchResponse.Meta(total,start,display),
                items.map { it.run { BlogSearchResponse.Document(title,description,link,bloggername,postdate) } }
        )
    }
}