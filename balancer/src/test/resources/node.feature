Feature:Node Features

  Scenario: Should start Aquila system
    When Balancer is started with '3' nodes
    Then Balancer should start '3' nodes, '6' linked backup nodes and '0' available backup nodes


  Scenario: Should save the key
    Given Aquila is started with '1' node
    When we try to save a "key" and "value"
    Then on fetching value using "key" it should return "value"
