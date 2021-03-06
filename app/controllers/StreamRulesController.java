package controllers;

import com.google.inject.Inject;
import org.graylog2.restclient.lib.APIException;
import org.graylog2.restclient.lib.ApiClient;
import lib.BreadcrumbList;
import org.graylog2.restclient.models.*;
import org.graylog2.restclient.models.api.requests.streams.CreateStreamRuleRequest;
import org.graylog2.restclient.models.api.responses.streams.CreateStreamRuleResponse;
import play.data.Form;
import play.mvc.Result;

import java.io.IOException;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class StreamRulesController extends AuthenticatedController {
    private static final Form<CreateStreamRuleRequest> createStreamRuleForm = Form.form(CreateStreamRuleRequest.class);

    @Inject
    private StreamService streamService;

    @Inject
    private StreamRuleService streamRuleService;

    public Result index(String streamId) {
        Stream stream;
        try {
            stream = streamService.get(streamId);
        } catch (APIException e) {
            String message = "Could not fetch stream rules. We expect HTTP 200, but got a HTTP " + e.getHttpCode() + ".";
            return status(504, views.html.errors.error.render(message, e, request()));
        } catch (IOException e) {
            return status(504, views.html.errors.error.render(ApiClient.ERROR_MSG_IO, e, request()));
        }

        return ok(views.html.streamrules.index.render(currentUser(), stream, stream.getStreamRules(), standardBreadcrumbs(stream)));
    }

    public Result create(String streamId) {
        Form<CreateStreamRuleRequest> form = createStreamRuleForm.bindFromRequest();
        CreateStreamRuleResponse response = null;

        try {
            CreateStreamRuleRequest csrr = form.get();
            response = streamRuleService.create(streamId, csrr);
            /*if (request().accepts("application/json"))
                return created(new Gson().toJson(response)).as("application/json");
            else {*/
                StreamRule streamRule = streamRuleService.get(streamId, response.streamrule_id);
                return created(views.html.partials.streamrules.list_item.render(streamRule));
            //}
        } catch (APIException e) {
            String message = "Could not create stream rule. We expected HTTP 201, but got a HTTP " + e.getHttpCode() + ".";
            return status(504, message);
        } catch (IOException e) {
            return status(504, e.toString());
        }

    }

    public Result update(String streamId, String streamRuleId) {
        Form<CreateStreamRuleRequest> form = createStreamRuleForm.bindFromRequest();
        CreateStreamRuleResponse response = null;

        try {
            CreateStreamRuleRequest csrr = form.get();
            response = streamRuleService.update(streamId, streamRuleId, csrr);
            /*if (request().accepts("application/json"))
                return created(new Gson().toJson(response)).as("application/json");
            else {*/
                StreamRule streamRule = streamRuleService.get(streamId, response.streamrule_id);
                return created(views.html.partials.streamrules.list_item.render(streamRule));
            //}
        } catch (APIException e) {
            String message = "Could not create stream rule. We expected HTTP 200, but got a HTTP " + e.getHttpCode() + ".";
            return status(504, message);
        } catch (IOException e) {
            return status(504, e.toString());
        }
    }

    public Result delete(String streamId, String streamRuleId){
        try {
            streamRuleService.delete(streamId, streamRuleId);
        } catch (APIException e) {
            String message = "Could not delete stream rule. We expect HTTP 204, but got a HTTP " + e.getHttpCode() + ".";
            return status(504, views.html.errors.error.render(message, e, request()));
        } catch (IOException e) {
            return status(504, views.html.errors.error.render(ApiClient.ERROR_MSG_IO, e, request()));
        }

        return ok();
    }

    private static BreadcrumbList standardBreadcrumbs(Stream stream) {
        BreadcrumbList bc = new BreadcrumbList();
        bc.addCrumb("All Streams", routes.StreamsController.index());
        bc.addCrumb("Stream: " + stream.getTitle(), null);

        return bc;
    }
}
