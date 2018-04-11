package com.kgaurav.balancer;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kgaurav.balancer.model.Request;
import com.kgaurav.balancer.model.RequestCode;
import com.kgaurav.balancer.model.Response;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;

public class NodeTest {
    private static final Logger LOGGER = Logger.getLogger(NodeTest.class);
    private static boolean started = false;
    private static final String aquilaAddress;
    private static int aquilaPort;
    private static final String ITEM_SAVE_RESPONSE = "Item is saved";
    private static final String ITEM_UPDATE_RESPONSE = "Item updated successfully";
    private Process appStartProcess;
    static {
        aquilaAddress = "0.0.0.0";
        aquilaPort = 9999;
    }

    @When("^Balancer is started with '(\\d+)' nodes$")
    public void balancerIsStartedWithNodes(int nodes) throws Throwable {
        String path = UtilTest.getApplicationBinaryLocation();
        //String command = "java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 "+path+" "+nodes;
        String command = "java -jar "+path+" "+nodes;
        LOGGER.info("Running command "+command);
        appStartProcess = UtilTest.runCommand(command);
        Assert.assertTrue("Application start failed", appStartProcess != null &&
                appStartProcess.isAlive());
        LOGGER.info("Sleeping for 10 sec");
        Thread.sleep(20000);
        started = true;
        /*String[] arg= new String[1];
        arg[0] = nodes+"";
        Application.main(arg);*/
    }

    @Then("^Balancer should start '(\\d+)' nodes, '(\\d+)' linked backup nodes and '(\\d+)' available backup nodes$")
    public void balancerShouldStartNodesLinkedBackupNodesAndAvailableBackupNodes(int mainNodeCount,
                        int linkedBackNodeCount, int backupNodeCount) throws Throwable {
        LOGGER.info("Step: Fetching info");
        Request request = new Request();
        request.setRequestCode(RequestCode.INFO);
        String requestData = new Gson().toJson(request);
        int mainNodesStarted = 0;
        int backupNodesStarted = 0;
        int availableBackupNodesStarted = 0;
        LOGGER.info("Connecting with server @"+aquilaAddress+":"+aquilaPort);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        JsonParser jsonParser = new JsonParser();
        JsonElement parse = jsonParser.parse(response.getData());
        JsonObject jsonObject = parse.getAsJsonObject();
        mainNodesStarted = jsonObject.get("main").getAsInt();
        backupNodesStarted = jsonObject.get("linked").getAsInt();
        availableBackupNodesStarted = jsonObject.get("available").getAsInt();
        Assert.assertEquals("Main nodes count is not matching", mainNodeCount, mainNodesStarted);
        Assert.assertEquals("Linked backup nodes count is not matching", linkedBackNodeCount,
                backupNodesStarted);
        Assert.assertEquals("Available bakup nodes count is not matching", availableBackupNodesStarted,
                backupNodeCount);
        //appStartProcess.destroyForcibly();
    }

    @When("^we try to save a \"([^\"]*)\" and \"([^\"]*)\"$")
    public void weTryToSaveAAnd(String key, String value) throws Throwable {
        LOGGER.info("Step: Saving value");
        Request request = new Request();
        request.setKey(key);
        request.setValue(value);
        request.setRequestCode(RequestCode.SET);
        String requestData = new Gson().toJson(request);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        Assert.assertNotNull("Response is null", response);
        Assert.assertNotNull("Response Data is null", response.getData());
        Assert.assertEquals("Response is unexpected", response.getData(), ITEM_SAVE_RESPONSE);

    }

    @Then("^on fetching value using \"([^\"]*)\" it should return \"([^\"]*)\"$")
    public void onFetchingValueUsingItShouldReturn(String key, String value) throws Throwable {
        LOGGER.info("Step: Fetching saved value by key");
        Request request = new Request();
        request.setKey(key);
        request.setRequestCode(RequestCode.GET);
        String requestData = new Gson().toJson(request);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        Assert.assertNotNull("Response is null", response);
        Assert.assertNotNull("Response Data is null", response.getData());
        Assert.assertEquals("Response is unexpected", value, response.getData());
    }

    @Given("^Aquila is started with '(\\d+)' node$")
    public void aquilaIsStartedWithNode(int arg0) throws Throwable {
        if(!started) {
            String path = UtilTest.getApplicationBinaryLocation();
            String command = "java -jar "+path+" "+1;
            appStartProcess = UtilTest.runCommand(command);
            Assert.assertTrue("Application start failed", appStartProcess != null &&
                    appStartProcess.isAlive());
            LOGGER.info("Sleeping for 10 sec");
            Thread.sleep(10000);
            started = true;
        }
    }

    @And("^There exists data with \"([^\"]*)\"$")
    public void thereExistsDataWith(String key) throws Throwable {
        Request request = new Request();
        request.setKey(key);
        request.setValue("Bogus text");
        request.setRequestCode(RequestCode.SET);
        String requestData = new Gson().toJson(request);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        Assert.assertNotNull("Response is null", response);
        Assert.assertNotNull("Response Data is null", response.getData());
        Assert.assertEquals("Response is unexpected", response.getData(), ITEM_SAVE_RESPONSE);
    }

    @When("^we try to update a \"([^\"]*)\" with \"([^\"]*)\"$")
    public void weTryToUpdateAWith(String key, String value) throws Throwable {
        LOGGER.info("Step: Update value");
        Request request = new Request();
        request.setKey(key);
        request.setValue(value);
        request.setRequestCode(RequestCode.PUT);
        String requestData = new Gson().toJson(request);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        Assert.assertNotNull("Response is null", response);
        Assert.assertNotNull("Response Data is null", response.getData());
        Assert.assertEquals("Response is unexpected", response.getData(), ITEM_UPDATE_RESPONSE);
    }

    @Then("^on deleting the data with\"([^\"]*)\", server should respond with message \"([^\"]*)\"$")
    public void onDeletingTheDataWithServerShouldRespondWithMessage(String key, String massage) throws Throwable {
        LOGGER.info("Step: delete value");
        Request request = new Request();
        request.setKey(key);
        request.setRequestCode(RequestCode.DEL);
        String requestData = new Gson().toJson(request);
        String responseFromAquila = UtilTest.sendAndReceiveDataToNode(aquilaAddress, aquilaPort, requestData);
        Response response = new Gson().fromJson(responseFromAquila, Response.class);
        Assert.assertNotNull("Response is null", response);
        Assert.assertNotNull("Response Data is null", response.getData());
        Assert.assertEquals("Response is unexpected", response.getData(), massage);
        appStartProcess.destroyForcibly();
    }
}
