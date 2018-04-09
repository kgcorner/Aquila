package com.kgaurav.balancer;


import com.google.gson.Gson;
import com.kgaurav.balancer.model.Request;
import com.kgaurav.balancer.model.RequestCode;
import com.kgaurav.balancer.model.Response;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.log4j.Logger;
import org.junit.Assert;

public class NodeTest {
    private static final Logger LOGGER = Logger.getLogger(NodeTest.class);
    private static boolean started = false;
    private static String aquilaAddress;
    private static int aquilaPort;
    private static final String ITEM_SAVE_RESPONSE = "Item is saved";


    @When("^Balancer is started with '(\\d+)' nodes$")
    public void balancerIsStartedWithNodes(int nodes) throws Throwable {
        String[] args = new String[1];
        args[0] = nodes+"";
        Application.main(args);
    }

    @Then("^Balancer should start '(\\d+)' nodes, '(\\d+)' linked backup nodes and '(\\d+)' available backup nodes$")
    public void balancerShouldStartNodesLinkedBackupNodesAndAvailableBackupNodes(int mainNodeCount,
                        int linkedBackNodeCount, int backupNodeCount) throws Throwable {
        BalancerServer balancerServer = BalancerServer.getInstance();
        int mainNodesStarted = balancerServer.getMainNodes().size();
        int backupNodesStarted = balancerServer.getLinkedBackupNodes().size();
        int availableBackupNodesStarted = balancerServer.getAvailableBackupNodes().size();
        Assert.assertEquals("Main nodes count is not matching", mainNodeCount, mainNodesStarted);
        Assert.assertEquals("Linked backup nodes count is not matching", linkedBackNodeCount,
                backupNodesStarted);
        Assert.assertEquals("Available bakup nodes count is not matching", availableBackupNodesStarted,
                backupNodeCount);

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
        Assert.assertEquals("Response is unexpected", response.getData(), value);
    }

    @Given("^Aquila is started with '(\\d+)' node$")
    public void aquilaIsStartedWithNode(int arg0) throws Throwable {
        if(!started) {
            LOGGER.info("Step: Starting Aquila");
            String[] args = new String[1];
            args[0] = 1+"";
            Application.main(args);
            String[] parts = Receptionist.getInstance().getRecenptionistAddress().split(":");
            aquilaAddress = parts[0];
            aquilaPort = Integer.parseInt(parts[1]);
            started = true;
        }
        else  {
            aquilaAddress = null;
            aquilaPort = 0;
        }
    }
}
