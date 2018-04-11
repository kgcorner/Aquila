Feature:Node Features

  Scenario: Should start Aquila system
    When Balancer is started with '1' nodes
    Then Balancer should start '1' nodes, '2' linked backup nodes and '0' available backup nodes


  Scenario: Should save the key
    Given Aquila is started with '1' node
    When we try to save a "key" and "value"
    Then on fetching value using "key" it should return "value"

  Scenario: Should update the key
    Given Aquila is started with '1' node
    And There exists data with "key"
    When we try to update a "key" with "new value"
    Then on fetching value using "key" it should return "new value"

  Scenario: Should delete the key
    Given Aquila is started with '1' node
    And There exists data with "key"
    Then on deleting the data with"key", server should respond with message "Item is deleted"
