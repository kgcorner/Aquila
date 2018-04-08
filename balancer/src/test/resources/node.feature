Feature:Node Features

  Scenario: Should start Aquila system
    When Balancer is started with '3' nodes
    Then Balancer should start '3' nodes, '6' linked backup nodes and '0' available backup nodes
