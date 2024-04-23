package com.example.bar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    final private String TAG = MainActivity.class.getCanonicalName();

    private TextView termView;
    private String term = "";
    private double result;
    private String resultString;
    private String fullEquation;
    private final LinkedList<String> history = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        termView = findViewById(R.id.term);

        //Initializing all Buttons, that just add a symbol to the term
        Button button1 = findViewById(R.id.button1);
        addTermStringToView(button1, '1');

        Button button2 = findViewById(R.id.button2);
        addTermStringToView(button2, '2');

        Button button3 = findViewById(R.id.button3);
        addTermStringToView(button3, '3');

        Button button4 = findViewById(R.id.button4);
        addTermStringToView(button4, '4');

        Button button5 = findViewById(R.id.button5);
        addTermStringToView(button5, '5');

        Button button6 = findViewById(R.id.button6);
        addTermStringToView(button6, '6');

        Button button7 = findViewById(R.id.button7);
        addTermStringToView(button7, '7');

        Button button8 = findViewById(R.id.button8);
        addTermStringToView(button8, '8');

        Button button9 = findViewById(R.id.button9);
        addTermStringToView(button9, '9');

        Button buttonAdd = findViewById(R.id.buttonAdd);
        addTermStringToView(buttonAdd, '+');

        Button buttonSubtract = findViewById(R.id.buttonSubtract);
        addTermStringToView(buttonSubtract, '-');

        Button buttonMultiply = findViewById(R.id.buttonMultiply);
        addTermStringToView(buttonMultiply, '*');

        Button buttonDivide = findViewById(R.id.buttonDivide);
        addTermStringToView(buttonDivide, '/');

        Button buttonOpenBracket = findViewById(R.id.buttonOpenBracket);
        addTermStringToView(buttonOpenBracket, '(');

        Button buttonCloseBracket = findViewById(R.id.buttonCloseBracket);
        addTermStringToView(buttonCloseBracket, ')');

        Button button0 = findViewById(R.id.button0);
        addTermStringToView(button0, '0');

        Button exportHistory = findViewById(R.id.exportHistory);
        exportHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "clicked export history");
                exportHistoryToFile(history);
            }
        });

        Button buttonEquals = findViewById(R.id.buttonEquals);
        buttonEquals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "clicked =");
                if(term.isEmpty()) {
                    return;
                }
                fullEquation = term + " = ";
                try {
                    if (term.contains("0/0")){
                        resultString = "division by 0";
                        fullEquation += "division by 0";
                        Toast.makeText(MainActivity.this, resultString,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        result = eval(term);
                        resultString = String.format("%f", result);
                        if(resultString.equals("Infinity")) {
                            resultString = "division by 0";
                            fullEquation += "division by 0";
                            Toast.makeText(MainActivity.this, resultString,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            fullEquation += resultString;
                        }
                    }
                } catch(RuntimeException e) {
                    fullEquation += "invalid term";
                    resultString = "invalid term";
                    Toast.makeText(MainActivity.this, "invalid term",
                            Toast.LENGTH_SHORT).show();
                } finally {
                    history.addFirst(fullEquation);
                    Log.i(TAG, "clicked show history");
                }

                addTextViewToScrollView(fullEquation);
                termView.setText(resultString);
                term = "";
            }
        });

        Button buttonDot = findViewById(R.id.buttonDot);
        buttonDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (term.contains(".")){
                    Log.e("Error", "Dot is already in term");
                } else {
                    term += ".";
                    termView.setText(term);
                    Log.i(TAG, "clicked and added dot");
                }
            }
        });

        Button buttonRemove = findViewById(R.id.buttonRemove);

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (term.length() > 1){
                    term = term.substring(0, term.length() - 1);
                    termView.setText(term);
                } else {
                    term = "";
                    termView.setText(term);
                }
                Log.i(TAG, "clicked remove");
            }
        });

        Button buttonRemoveAll = findViewById(R.id.buttonRemoveAll);

        buttonRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                term = "";
                termView.setText(term);
                Log.i(TAG, "clicked RemoveAll");
            }
        });
    }

    private void addTextViewToScrollView(String text) {

        // Adds a textview with the last result at the first position of the LinearLayout of the ScrollView.
        LinearLayout linearLayout = findViewById(R.id.historyLinearLayout);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(50);
        textView.setPadding(20,0,0,0);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(textView, 0);
    }
    private void addTermStringToView(Button button, Character symbol) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.format("clicked %s", symbol));
                term += symbol;
                termView.setText(term);
            }
        });

    }
    private void exportHistoryToFile(LinkedList<String> history) {
        try {
            String filename = "history_" + new Date().toString().replace(" ", "-").toLowerCase() + ".txt";

            // construct file metadata
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            // request to write the file to the media store
            ContentResolver resolver = this.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                Log.i(TAG, "Failed to obtain an uri to write to");
                return;
            }

            OutputStream outputStream = resolver.openOutputStream(uri);

            if (outputStream == null) {
                Log.i(TAG, "Failed to obtain an output stream to write to");
                return;
            }

            Log.i(TAG, "Writing history to media store");

            PrintWriter out = new PrintWriter(outputStream);
            for (String s : history) {
                out.println(s);
            }
            out.close();
            Log.i(TAG, "History is written to media store");
        } catch (IOException e) {
            Log.i(TAG, "Failed to export history:\n" + e);
        }
    }

    // We have taken the implementation of the eval function from this post:
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}