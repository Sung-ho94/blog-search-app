package com.kakao.task.service

import com.kakao.task.domain.searchKeywordLog.SearchKeywordLog
import com.kakao.task.domain.searchKeywordLog.SearchKeywordLogRepository
import com.kakao.task.externalApi.BlogSearchResponse
import com.kakao.task.externalApi.KakaoBlogSearch
import com.kakao.task.externalApi.NaverBlogSearch
import com.kakao.task.externalApi.SearchRequest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class BlogSearchServiceUnitTest : DescribeSpec({
    val kakaoBlogSearch: KakaoBlogSearch = mockk()
    val naverBlogSearch: NaverBlogSearch = mockk()
    val searchKeywordLogRepository: SearchKeywordLogRepository = mockk()

    val blogSearchService = BlogSearchService(kakaoBlogSearch, naverBlogSearch, searchKeywordLogRepository)

    describe("블로그 검색 테스트 "){

        val searchRequest = SearchRequest("query",SearchRequest.Sort.accuracy,1,10)
        val blogSearchResponse = BlogSearchResponse(
                BlogSearchResponse.Meta(10,10,false),
                listOf(BlogSearchResponse.Document("title","contents","url","blogname","datetime"))
        )
        every { searchKeywordLogRepository.save(any()) } returns Unit

        it("카카오 블로그 검색 테스트"){
            val searchRequestSlot = slot<SearchRequest>()
            every { kakaoBlogSearch.getBlogData(capture(searchRequestSlot)) }returns blogSearchResponse

            blogSearchService.getData(searchRequest)

            verify(exactly = 1) { kakaoBlogSearch.getBlogData(capture(searchRequestSlot)) }
            verify(exactly = 0) { naverBlogSearch.getBlogData(any()) }
            val capturedRequest = searchRequestSlot.captured
            capturedRequest.let {
                it.query shouldBe searchRequest.query
                it.page shouldBe searchRequest.page
                it.size shouldBe searchRequest.size
                it.sort shouldBe searchRequest.sort
            }
        }

        it("카카오 검색 실패시 네이버 블로그 검색 테스트"){
            val searchRequestSlot = slot<SearchRequest>()
            every { kakaoBlogSearch.getBlogData(any()) } throws IllegalArgumentException()
            every { naverBlogSearch.getBlogData(capture(searchRequestSlot)) } returns blogSearchResponse

            blogSearchService.getData(searchRequest)

            verify(exactly = 1) { kakaoBlogSearch.getBlogData(any()) }
            verify(exactly = 1) { naverBlogSearch.getBlogData(capture(searchRequestSlot)) }

            val capturedRequest = searchRequestSlot.captured
            capturedRequest.let {
                it.query shouldBe searchRequest.query
                it.page shouldBe searchRequest.page
                it.size shouldBe searchRequest.size
                it.sort shouldBe searchRequest.sort
            }
        }

        it("검색 로그 저장 테스트"){
            val searchKeywordLogSlot = slot<SearchKeywordLog>()
            every { kakaoBlogSearch.getBlogData(any()) } returns blogSearchResponse
            every { searchKeywordLogRepository.save(capture(searchKeywordLogSlot)) } returns Unit

            blogSearchService.getData(searchRequest)


            verify(exactly = 1) { searchKeywordLogRepository.save(capture(searchKeywordLogSlot)) }
            val capturedRequest = searchKeywordLogSlot.captured
            capturedRequest.keyword.value shouldBe  searchRequest.query
        }

    }
})
