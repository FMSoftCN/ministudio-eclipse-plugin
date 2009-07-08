/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.minigui.eclipse.cdt.mstudio;

import java.net.*;
import java.io.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.core.runtime.Status;

public class MStudioSocketServerThread extends Thread {
    public Socket           socket;
    public int              Started = 0;
    private static int      port = 5010;			

    private ServerSocket    server;
    private InputStream     input;
    private OutputStream    output;
    private StringBuffer    request = new StringBuffer();
    private String          content;
    private byte            crlf13 = (byte)13; //'\r'
    private byte            crlf10 = (byte)10;  //'\n'

    private static MStudioSocketServerThread instance = new MStudioSocketServerThread(port);

    private MStudioSocketServerThread(int port)
    {
        try {
            server = new ServerSocket(port);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    public static MStudioSocketServerThread getInstance()
    {
        return instance;
    }
    
    public void run() 
    {
        Started = 1;

        try {
            while (true) 
            {
                socket = server.accept();
                parseData();       
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    GUISEND\r\n
    file:test.java\r\n
    key:int MiniGUIMain(\r\n
    \r\n
    */
    private void parseData() 
    {
        byte[] crlf = new byte[1];
        int crlfnum = 0;

        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            while (input.read(crlf)!=-1) {

                if (crlf[0] == crlf13 || crlf[0] == crlf10) {
                    crlfnum ++;
                } else {
                    crlfnum = 0;
                }

                request = request.append (new String (crlf, 0, 1));

                if (crlfnum == 4) {
                    processData();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processData () 
    {
        try {
            sendAck();
            recvAck();
        } catch (IOException e) {
            e.printStackTrace();
        }

        content = new String(request);
        request.delete(0, request.length());

        if (content.startsWith("GUISEND")) {
            UIJob refreshJob = new UIJob(content) {
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    try {
                        GoToFunc(getFunc(), getFileName());     
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    return Status.OK_STATUS;
                }
            };
            refreshJob.schedule();
        }
    }
    
    private void GoToFunc (String func, String fileName) throws CoreException, IOException 
    {
        Path path = new Path(fileName);
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        IProject project = file.getProject();		
        int offset = 0;

        project.refreshLocal(IResource.DEPTH_INFINITE, null);
        offset = indexOfInFile (func, file);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        try {
            ITextEditor editor = (ITextEditor)IDE.openEditor(page, file, true);
            editor.selectAndReveal(offset, 0);
        } 
        catch (PartInitException e) {
            e.printStackTrace();
        }
    }

    private int indexOfInFile(String func, IFile file) throws CoreException, IOException {
        Reader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));

        try {
            int c = 0;
            int offset = 0;
            StringBuffer buf = new StringBuffer();

            while ((c=reader.read()) >= 0) {
                buf.append((char)c);
                if (c == '\n') {
                    int idx = buf.indexOf(func);
                    if (idx >= 0) {
                        return idx+offset;
                    }
                    offset+=buf.length();
                    buf.setLength(0);
                }
            }

            int idx = buf.indexOf(func);
            if (idx >= 0) {
                return idx+offset;
            }
            return -1;
        }
        finally {
            reader.close();
        }
    }

    private String getFileName() {
        return getString ("file:", content);
    }

    private String getFunc() {
        return getString ("key:", content);
    }

    private String getString (String key, String content) {
        int index = content.indexOf(key);
        byte req[] = content.getBytes();

        if (index != -1) {
            StringBuffer sb = new StringBuffer();

            for (int i = (index + key.length()); ; i++) {
                if (req[i] != (byte)13 && req[i] != (byte)10) {
                    sb.append ((char)req[i]);
                }
                else
                    break;
            }
        //System.out.println(key + sb.toString());

            return sb.toString ();
        }

        return null;
    }

    private void sendAck() throws IOException {
        int ack = 0;
        output.write(ack);
        output.flush();
    }

    private void recvAck() throws IOException {
        input.read();
    }

    public void closeSocket() {
        try {
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
