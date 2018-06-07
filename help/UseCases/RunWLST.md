## Run WLST

In this example, the objective is to print "Hello
World!" in an output device by using the <i>Supplied File</i> option
with an existing script file in a path or the <i>New
Script File</i> option with embedded code for the
<i>Script File Source</i> parameter.

<ol>
    <li>Go to the Plugin Manager and select the RunWLST procedure. </li>
    <li>Select the <i>Supplied File</i> or the <i>New Script File</i> option as the <i>Script File Source</i>,
    and enter the following parameters:<br />
    This shows the parameters with the <i>Supplied File</i> option:<br />
    <img src="../../plugins/EC-WebLogic/images/RunWLST/EC-WLSRunWLST2.png" /> <br />
    This shows the parameters with the <i>New Script File</i> option:<br />
    <img src="../../plugins/EC-WebLogic/images/RunWLST/EC-WLSRunWLST3.png" />
    </li>
</ol>

### Output
After the job runs, you can view the results on the Job Details page in ElectricFlow. The "Hello World!"

message is successfully printed with either of the <i>Script File Source</i> options (<i>Supplied File</i> or
<i>New Script File</i>).

<img src="../../plugins/EC-WebLogic/images/RunWLST/EC-WLSRunWLST4.png" />
<p>In the <b>RunWLST</b> step, click the Log button to see the diagnostic information similar to this:</p>
<img src="../../plugins/EC-WebLogic/images/RunWLST/EC-WLSRunWLST5.png" />
<p>This is the <i>HelloWorld.jython</i> file used in the <i>Supplied File</i> option:</p>
<img src="../../plugins/EC-WebLogic/images/RunWLST/EC-WLSRunWLST6.png" />
