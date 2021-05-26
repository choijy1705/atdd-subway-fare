package wooteco.subway.path;

import com.google.common.collect.Lists;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.auth.dto.TokenResponse;
import wooteco.subway.line.dto.LineResponse;
import wooteco.subway.path.dto.PathResponse;
import wooteco.subway.station.dto.StationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static wooteco.subway.auth.AuthAcceptanceTest.로그인되어_있음;
import static wooteco.subway.line.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static wooteco.subway.line.SectionAcceptanceTest.지하철_구간_등록되어_있음;
import static wooteco.subway.member.MemberAcceptanceTest.회원_생성됨;
import static wooteco.subway.member.MemberAcceptanceTest.회원_생성을_요청;
import static wooteco.subway.station.StationAcceptanceTest.지하철역_등록되어_있음;

@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    private static final String EMAIL1 = "email1@email.com";
    private static final String EMAIL2 = "email2@email.com";
    private static final String EMAIL3 = "email3@email.com";
    private static final String EMAIL4 = "email4@email.com";
    private static final String PASSWORD = "password";
    private static final int AGE_UNDER_6 = 5;
    private static final int AGE_IN_BETWEEN_6_TO_13 = 9;
    private static final int AGE_IN_BETWEEN_13_TO_19 = 18;
    private static final int AGE_ADULT = 20;

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private LineResponse 사호선;
    private LineResponse 오호선;
    private LineResponse 육호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 멀리있는역;
    private StationResponse 잠실역;
    private StationResponse 강변역;
    private StationResponse 구의역;
    private StationResponse 성수역;
    private StationResponse 뚝섬역;
    TokenResponse 초등학생;
    TokenResponse 중고등학생;
    TokenResponse 아기;
    TokenResponse 성인;

    /*
     * 교대역    --- *2호선*10km---   강남역
     * |                        |
     * *3호선*3km                   *신분당선*10km
     * |                        |
     * 남부터미널역  --- *3호선*2km ---   양재  --50km--  멀리있는역
     *
     *     강남1
     *     양재2
     *     교대3
     *     남부4
     *     멀리5
     */

    /*
     * 잠실역    --- *4호선5km900---   강변역
     * |                        |
     * *6호선*8km1500         *5호선*6km500
     * |                        |
     * 성수역  --- *6호선*7km1500 ---   구의역  -- 6호선50km  --- 뚝섬
     *
     *     잠실6
     *     강변7
     *     구의8
     *     성수9
     */

    @BeforeEach
    public void setUp() {
        super.setUp();

        ExtractableResponse<Response> createResponse1 = 회원_생성을_요청(EMAIL1, PASSWORD, AGE_UNDER_6);
        회원_생성됨(createResponse1);
        아기 = 로그인되어_있음(EMAIL1, PASSWORD);
        ExtractableResponse<Response> createResponse2 = 회원_생성을_요청(EMAIL2, PASSWORD, AGE_IN_BETWEEN_6_TO_13);
        회원_생성됨(createResponse2);
        초등학생 = 로그인되어_있음(EMAIL2, PASSWORD);
        ExtractableResponse<Response> createResponse3 = 회원_생성을_요청(EMAIL3, PASSWORD, AGE_IN_BETWEEN_13_TO_19);
        회원_생성됨(createResponse3);
        중고등학생 = 로그인되어_있음(EMAIL3, PASSWORD);
        ExtractableResponse<Response> createResponse4 = 회원_생성을_요청(EMAIL4, PASSWORD, AGE_ADULT);
        회원_생성됨(createResponse4);
        성인 = 로그인되어_있음(EMAIL4, PASSWORD);

        강남역 = 지하철역_등록되어_있음("강남역");
        양재역 = 지하철역_등록되어_있음("양재역");
        교대역 = 지하철역_등록되어_있음("교대역");
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역");
        멀리있는역 = 지하철역_등록되어_있음("멀리있는역");

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10, 0);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10, 0);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 5, 0);

        지하철_구간_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);
        지하철_구간_등록되어_있음(삼호선, 양재역, 멀리있는역, 50);

        잠실역 = 지하철역_등록되어_있음("잠실역");
        강변역 = 지하철역_등록되어_있음("강변역");
        구의역 = 지하철역_등록되어_있음("구의역");
        성수역 = 지하철역_등록되어_있음("성수역");
        뚝섬역 = 지하철역_등록되어_있음("뚝섬역");
        사호선 = 지하철_노선_등록되어_있음("사호선", "bg-red-600", 잠실역, 강변역, 5, 900);
        오호선 = 지하철_노선_등록되어_있음("오호선", "bg-red-600", 강변역, 구의역, 6, 500);
        육호선 = 지하철_노선_등록되어_있음("육호선", "bg-red-600", 구의역, 성수역, 7, 1500);

        지하철_구간_등록되어_있음(육호선, 성수역, 잠실역, 8);
        지하철_구간_등록되어_있음(육호선, 뚝섬역, 구의역, 50);
    }

    @DisplayName("기본경로, 0~6세")
    @Test
    void findPathBaby() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(아기, 3L, 2L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리가_응답됨(response, 5);
        총_요금이_응답됨(response, 0);
    }

    @DisplayName("기본경로, 6~13세")
    @Test
    void findPathChild() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(초등학생,3L, 2L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리가_응답됨(response, 5);
        총_요금이_응답됨(response, 450);
    }

    @DisplayName("기본경로, 13~19세")
    @Test
    void findPathStudent() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(중고등학생, 3L, 2L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리가_응답됨(response, 5);
        총_요금이_응답됨(response, 720);
    }

    @DisplayName("기본경로, 성인")
    @Test
    void findPathAdult() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(성인, 3L, 2L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리가_응답됨(response, 5);
        총_요금이_응답됨(response, 1250);
    }

    @DisplayName("기본경로, 로그인 안한 경우")
    @Test
    void findPath() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(new TokenResponse(""), 3L, 2L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역));
        총_거리가_응답됨(response, 5);
        총_요금이_응답됨(response, 1250);
    }

    @DisplayName("비회원, 55km 일경우 추가운임 확인, 교대역 -> 멀리있는역")
    @Test
    void findPath55km() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(new TokenResponse(""), 3L, 5L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(교대역, 남부터미널역, 양재역, 멀리있는역));
        총_거리가_응답됨(response, 55);
        총_요금이_응답됨(response, 2150);
    }

    @DisplayName("비회원, 12Km 일 경우 거리에 따른 추가운임 확인, 강냠역 -> 남부터미널역")
    @Test
    void findPath12Km() {
        //when
        ExtractableResponse<Response> response = 거리_경로_조회_요청(new TokenResponse(""), 1L, 4L);
        //then
        적절한_경로_응답됨(response, Lists.newArrayList(강남역, 양재역, 남부터미널역));
        총_거리가_응답됨(response, 12);
        총_요금이_응답됨(response, 1350);
    }

    public static ExtractableResponse<Response> 거리_경로_조회_요청(TokenResponse tokenResponse, long source, long target) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/paths?source={sourceId}&target={targetId}", source, target)
                .then().log().all()
                .extract();
    }

    public static void 적절한_경로_응답됨(ExtractableResponse<Response> response, ArrayList<StationResponse> expectedPath) {
        PathResponse pathResponse = response.as(PathResponse.class);

        List<Long> stationIds = pathResponse.getStations().stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());

        List<Long> expectedPathIds = expectedPath.stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());

        assertThat(stationIds).containsExactlyElementsOf(expectedPathIds);
    }

    public static void 총_거리가_응답됨(ExtractableResponse<Response> response, int totalDistance) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse.getDistance()).isEqualTo(totalDistance);
    }

    public static void 총_요금이_응답됨(ExtractableResponse<Response> response, int fare) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse.getFare()).isEqualTo(fare);
    }
}
