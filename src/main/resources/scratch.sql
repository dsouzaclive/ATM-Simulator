select printf('%s %s %s', c.firstName, c.middleName, c.lastName) as CustomerName, a.customerID as CustomerID, count(a.customerID) as NoOfAccounts
from AccountDetails a inner join Customer c on a.customerID = c.customerID
group by a.customerID
having count(a.customerID)>1;