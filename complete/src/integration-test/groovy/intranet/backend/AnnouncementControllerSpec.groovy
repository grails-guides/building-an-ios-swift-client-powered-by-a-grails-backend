package intranet.backend

import grails.testing.mixin.integration.Integration
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import spock.lang.Specification

@Integration
class AnnouncementControllerSpec extends Specification {

    def "test body is present in announcements json payload of Api 1.0"() {
        given:
        HttpClient client = HttpClient.create("http://localhost:${serverPort}/".toURL())
        HttpRequest request = HttpRequest.GET(UriBuilder.of('/announcements').build())
                .header("Accept-Version", "1.0");

        when: 'Requesting announcements for version 1.0'
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)
        JSONArray jsonArray = new JSONArray(resp.body())

        then: 'the request was successful'
        resp.status == HttpStatus.OK // <3>

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == 'application/json;charset=UTF-8'

        and: 'json payload contains an array of announcements with id, title and body'
        jsonArray.myArrayList.each {
            assert it.id
            assert it.title
            assert it.body // <4>
        }
    }

    def "test body is NOT present in announcements json payload of Api 2.0"() {
        given:
        HttpClient client = HttpClient.create("http://localhost:${serverPort}/".toURL())
        HttpRequest request = HttpRequest.GET(UriBuilder.of('/announcements/').build())
                .header("Accept-Version", "2.0");

        when: 'Requesting announcements for version 2.0'
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)
        JSONArray jsonArray = new JSONArray(resp.body())

        then: 'the request was successful'
        resp.status == HttpStatus.OK

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == 'application/json;charset=UTF-8'

        and: 'json payload contains an array of annoucements with id, title'
        jsonArray.myArrayList.each {
            assert it.id
            assert it.title
            assert !it.body // <2>
        }
    }

    def "test detail of an announcement contains body in both version 1.0 and 2.0"() {
        given:
        def announcementId = 2 as Long
        HttpClient client = HttpClient.create("http://localhost:${serverPort}/".toURL())
        HttpRequest request = HttpRequest.GET(UriBuilder.of("/announcements/${announcementId}").build())
                .header("Accept-Version", "1.0");

        when: 'Requesting announcements for version 1.0'
        HttpResponse<String> resp = client.toBlocking().exchange(request, String)
        JSONObject jsonObject = new JSONObject(resp.body())

        then: 'the request was successful'
        resp.status == HttpStatus.OK

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == 'application/json;charset=UTF-8'

        and: 'json payload contains the complete announcement'
        jsonObject.id
        jsonObject.title
        jsonObject.body

        when: 'Requesting announcements for version 2.0'
        request = HttpRequest.GET(UriBuilder.of("/announcements/${announcementId}").build())
                .header("Accept-Version", "2.0");
        resp = client.toBlocking().exchange(request, String)
        jsonObject = new JSONObject(resp.body())

        then: 'the request was successful'
        resp.status == HttpStatus.OK

        and: 'the response is a JSON Payload'
        resp.headers.get('Content-Type') == 'application/json;charset=UTF-8'

        and: 'json payload contains the complete annoucement'
        jsonObject.id
        jsonObject.title
        jsonObject.body
    }
}
