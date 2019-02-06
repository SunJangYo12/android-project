package com.cpu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

public class MainTerminal extends Activity {
    private TextView output;
    private EditText input;
    private Button submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_terminal);

        output= (TextView)findViewById(R.id.terminal_output);
        input= (EditText)findViewById(R.id.terminal_input);
        submit= (Button)findViewById(R.id.terminal_submit);

        input.setText("/system/bin/ps");
		String commandOutput = runShellCommand(input.getText().toString());
        output.setText(commandOutput);
        
        submit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v){
					String commandOutput = runShellCommand(input.getText().toString());
        			output.setText(commandOutput);
				}
		});
    }

	private String runShellCommand(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(reader);
		    int numRead;
		    char[] buffer = new char[5000];
		    StringBuffer commandOutput = new StringBuffer();
		    while ((numRead = bufferedReader.read(buffer)) > 0) {
		        commandOutput.append(buffer, 0, numRead);
		    }
		    bufferedReader.close();
		    process.waitFor();

		    return commandOutput.toString();
		} catch (IOException e) {

		    throw new RuntimeException(e);

		} catch (InterruptedException e) {

		    throw new RuntimeException(e);
		}

	}
}
