package crowdsense.cimoninterface.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import crowdsense.cimoninterface.util.DatabaseUtil;
import crowdsense.cimoninterface.util.Response;
import crowdsense.cimoninterface.util.ServiceUtil;

@Path("/study")
public class StudyService {

	private Connection connection = null;
	private PreparedStatement preparedStatement = null;

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Study> getStudyList(@QueryParam("uuid") String uuid, @QueryParam("email") String email) {
		ArrayList<Study> studyList = new ArrayList<>();

		if (ServiceUtil.isEmptyString(uuid) || ServiceUtil.isEmptyString(email)) {
			return studyList;
		}

		try {
			connection = DatabaseUtil.connectToDatabase();
			String query = "select * from mcs.study where state=1";
			preparedStatement = connection.prepareStatement(query);

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Study study = new Study();

				long id = resultSet.getLong("id");
				String name = resultSet.getString("name");
				String description = resultSet.getString("description");
				int state = resultSet.getInt("state");
				String modificationTime = resultSet.getString("modification_time");

				study.setId(id);
				study.setName(name);
				study.setDescription(description);
				study.setState(state);
				study.setModificationTime(modificationTime);

				studyList.add(study);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException ignore) {
				}

		}
		return studyList;

	}

	@GET
	@Path("{studyId}/surveys/published")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<SurveySummary> getPublishedSurveys(@PathParam("studyId") long studyId) {
		ArrayList<SurveySummary> surveyList = new ArrayList<>();

		try {
			connection = DatabaseUtil.connectToDatabase();
			String query = "select * from mcs.survey_summary where study_id=" + studyId + " and state=2";
			preparedStatement = connection.prepareStatement(query);

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				SurveySummary survey = new SurveySummary();

				long surveyId = resultSet.getLong("id");
				String name = resultSet.getString("name");
				String description = resultSet.getString("description");
				String publishTime = resultSet.getString("publish_time");
				String publishTimezone = resultSet.getString("publish_time_zone");
				int publishedVersion = resultSet.getInt("published_version");
				int state = resultSet.getInt("state");
				String modificationTime = resultSet.getString("modification_time");
				String startTime = resultSet.getString("start_time");
				String startTimezone = resultSet.getString("start_time_zone");
				String endTime = resultSet.getString("end_time");
				String endTimezone = resultSet.getString("end_time_zone");
				String schedule = resultSet.getString("schedule");

				survey.setId(surveyId);
				survey.setStudyId(studyId);
				survey.setName(name);
				survey.setDescription(description);
				survey.setPublishTime(publishTime);
				survey.setPublishTimeZone(publishTimezone);
				survey.setPublishedVersion(publishedVersion);
				survey.setState(state);
				survey.setModificationTime(modificationTime);
				survey.setStartTime(startTime);
				survey.setStartTimeZone(startTimezone);
				survey.setEndTime(endTime);
				survey.setEndTimeZone(endTimezone);
				survey.setSchedule(schedule);

				surveyList.add(survey);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException ignore) {
				}

		}
		return surveyList;

	}

	@GET
	@Path("{studyId}/survey/{surveyId}/tasklist")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<SurveyTask> getTaskList(@PathParam("studyId") long studyId, @PathParam("surveyId") long surveyId) {
		ArrayList<SurveyTask> taskList = new ArrayList<>();

		try {
			connection = DatabaseUtil.connectToDatabase();
			String query = "select published_version from mcs.survey_summary where id=" + surveyId + " and study_id="
					+ studyId +" and state=2";
			preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				int version = resultSet.getInt("published_version");
				if (version > 0) {
					query = "select * from mcs.survey_task where study_id=" + studyId + " and survey_id=" + surveyId
							+ " and version=" + version + " and is_active=1 order by order_id";
					preparedStatement = connection.prepareStatement(query);
					resultSet = preparedStatement.executeQuery();
					while (resultSet.next()) {
						SurveyTask task = new SurveyTask();
						int taskId = resultSet.getInt("task_id");
						String taskText = resultSet.getString("task_text");
						String type = resultSet.getString("type");
						String possibleInput = resultSet.getString("possible_input");
						int orderId = resultSet.getInt("order_id");
						int isActive = resultSet.getInt("is_active");
						int isRequired = resultSet.getInt("is_required");
						int parentTaskId = resultSet.getInt("parent_task_id");
						int hasChild = resultSet.getInt("has_child");
						String childTriggeringInput = resultSet.getString("child_triggering_input");
						String defaultInput = resultSet.getString("default_input");

						task.setStudyId(studyId);
						task.setSurveyId(surveyId);
						task.setVersion(version);
						task.setTaskId(taskId);
						task.setTaskText(taskText);
						task.setType(type);
						task.setPossibleInput(possibleInput);
						task.setOrderId(orderId);
						task.setIsActive(isActive);
						task.setIsRequired(isRequired);
						task.setParentTaskId(parentTaskId);
						task.setHasChild(hasChild);
						task.setChildTriggeringInput(childTriggeringInput);
						task.setDefaultInput(defaultInput);

						taskList.add(task);
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException ignore) {
				}

		}
		return taskList;

	}
	
	@POST
	@Path("/survey/response")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveSurveyResponse(ArrayList<SurveyResponse> responseList){
		//System.out.println("request:"+ responses);
		//String input = (String) obj.get("input");
		System.out.println("input:"+ responseList.size());

		Response serviceResponse = new Response();
		//ArrayList<SurveyResponse> responseList = new Gson().fromJson(responses, new TypeToken<ArrayList<SurveyResponse>>(){}.getType());
		//ArrayList<SurveyResponse> responseList = jaxbElement.getValue();
		//for(int i=0;i<responseList.getResponseList().size();i++){
			//System.out.println("response type:" + responseList.getResponseList().get(i).getAnswerType() + ", response:"+ responseList.getResponseList().get(i).getAnswer());
		//}
		return serviceResponse;
	}

	/*
	 * @GET
	 * 
	 * @Path("verify")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * verifyToken(@QueryParam("uuid") String uuid, @QueryParam("email") String
	 * email, @QueryParam("token") String token){ Response response = new
	 * Response();
	 * 
	 * if(ServiceUtil.isEmptyString(token)){
	 * response.setMessage("Invalid token"); }
	 * 
	 * if(ServiceUtil.isEmptyString(uuid) || ServiceUtil.isEmptyString(email)){
	 * response.setCode(-2); response.setMessage("Invalid input"); return
	 * response; }
	 * 
	 * try { connection = DatabaseUtil.connectToDatabase();
	 * 
	 * System.out.println("email:"+ email + ", uuid:" + uuid);
	 * 
	 * preparedStatement = connection.
	 * prepareStatement("select token from cimon.mobile_users where email=? and device_uuid=?"
	 * ); preparedStatement.setString(1, email); preparedStatement.setString(2,
	 * uuid);
	 * 
	 * resultSet = preparedStatement.executeQuery(); if(resultSet.next()){
	 * String dbToken = resultSet.getString("token");
	 * System.out.println("token from db:"+ dbToken + ", from client:"+ token);
	 * if(dbToken.equals(token)){ System.out.println("token match"); try {
	 * preparedStatement = connection.
	 * prepareStatement("update cimon.mobile_users set state=1 where email=? and device_uuid=?"
	 * ); preparedStatement.setString(1, email); preparedStatement.setString(2,
	 * uuid);
	 * 
	 * preparedStatement.executeUpdate();
	 * System.out.println("going to update db"); } catch (Exception e) { //
	 * TODO: handle exception e.printStackTrace(); } response.setCode(0); }
	 * else{ System.out.println("token does not match"); response.setCode(-3);
	 * response.setMessage("Token mismatch"); } }else{ response.setCode(-2);
	 * response.setMessage("Invalid input"); }
	 * 
	 * } catch (Exception ex) { ex.printStackTrace(); response.setCode(-9);
	 * response.setMessage("Service not available"); } finally { if (connection
	 * != null) try { connection.close(); } catch (SQLException ignore) { }
	 * 
	 * } return response; }
	 */

}
