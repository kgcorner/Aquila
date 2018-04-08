package com.kgaurav.balancer;


import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

public class NodeTest {

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
}
