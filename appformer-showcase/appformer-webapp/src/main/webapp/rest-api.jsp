<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>jBPM Execution server REST API</title>
  <style type="text/css">
    table.t-table {
      border: 1px solid #CCC;
      font-size: 12px;
    }
    .t-table td {
      padding: 4px;
      margin: 3px;
      border: 1px solid #ccc;
    }
    .t-table th {
      background-color: #104E8B;
      color: #FFF;
      font-weight: bold;
    }
  </style>
 </head>
<body>
<h2>REST API description</h2>
Entry point for REST interface is at <%= request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+""+request.getContextPath()+"/"%>rest/
<br/>

<h3>URL parameters used in the REST resource URLs below:</h3>

<h3>Available resources for managing repositories, projects and organizational units:</h3>

<table class="t-table" width="100%">
  <tr class="t-firstrow">
    <th>Resource</th>
    <th>HTTP Op</th>
    <th>Description</th>
  </tr>
  <tr><td>/jobs/{jobId}</td><td>GET</td><td>get the status of a job</td></tr>
  <tr><td>/jobs/{jobId}</td><td>DELETE</td><td>attempt to remove a job before it has been executed</td></tr>
  <tr><td>/organizationalunits</td><td>GET</td><td>get details for all organizational units</td></tr>
  <tr><td>/organizationalunits</td><td>POST</td><td>create an organizational unit</td></tr>
  <tr><td>/organizationalunits/{organizationalUnitName}</td><td>GET</td><td>get the details for an organizational unit</td></tr>
  <tr><td>/organizationalunits/{organizationalUnitName}</td><td>DELETE</td><td>delete an organizational unit</td></tr>
  <tr><td>/organizationalunits/{organizationalUnitName}/</td><td>POST</td><td>update an existing organizational unit</td></tr>
  <tr><td>/organizationalunits/{organizationalUnitName}/repositories/{repositoryName}</td><td>POST</td><td>add a repository to an organizational unit</td></tr>
  <tr><td>/organizationalunits/{organizationalUnitName}/repositories/{repositoryName}</td><td>DELETE</td><td>remove a repository from an organizational unit</td></tr>
  <tr><td>/repositories</td><td>GET</td><td>get details for  repositories</td></tr>
  <tr><td>/repositories</td><td>POST</td><td>create a repository</td></tr>
  <tr><td>/repositories/{repositoryName}</td><td>DELETE</td><td>delete a repository</td></tr>
  <tr><td>/repositories/{repositoryName}</td><td>GET</td><td>get repository details</td></tr>
  <tr><td>/repositories/{repositoryName}/projects</td><td>GET</td><td>get details for all projects in a repository</td></tr>
  <tr><td>/repositories/{repositoryName}/projects</td><td>POST</td><td>create a project in a repository</td></tr>
  <tr><td>/repositories/{repositoryName}/projects/{projectName}</td><td>DELETE</td><td>remove a project</td></tr>
  <tr><td>/repositories/{repositoryName}/projects/{projectName}/maven/compile</td><td>POST</td><td>build a project</td></tr>
  <tr><td>/repositories/{repositoryName}/projects/{projectName}/maven/test</td><td>POST</td><td>run all tests in a project</td></tr>
  <tr><td>/repositories/{repositoryName}/projects/{projectName}/maven/install</td><td>POST</td><td>build and deploy a project</td></tr>
  <tr><td>/repositories/{repositoryName}/projects/{projectName}/maven/deploy</td><td>POST</td><td>build and deploy a project [<em>deprecated</em>]</td></tr>
</table>

</body>
</html>
