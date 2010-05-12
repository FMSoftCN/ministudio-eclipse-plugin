/*
 ============================================================================
 Name        : $(baseName).c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Main Window in MiniGUI
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>
#include <sys/types.h>
#include <sys/wait.h>

#include <minigui/common.h>
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>

static BOOL quit = FALSE;

static void on_new_del_client (int op, int cli)
{       
    static int nr_clients = 0;

    if (op == LCO_NEW_CLIENT) {
        nr_clients ++;
    }   
    else if (op == LCO_DEL_CLIENT) {
        nr_clients --;
        if (nr_clients == 0) {
            printf ("There is no any client, I will quit.\n");
            quit = TRUE;
        }               
        else if (nr_clients < 0) {
            printf ("Serious error: nr_clients less than zero.\n");
        }
    }
    else
        printf ("Serious error: incorrect operations.\n");
}

static pid_t exec_app (const char* file_name, const char* app_name)
{
    pid_t pid = 0;

    if ((pid = vfork ()) > 0) {
        fprintf (stderr, "new child, pid: %d.\n", pid);
    }
    else if (pid == 0) {
        execl (file_name, app_name, NULL);
        perror ("execl");
        _exit (1);
    }
    else {
        perror ("vfork");
    }

    return pid;
}
int MiniGUIMain (int argc, const char* argv[])
{
    MSG Msg;
    struct sigaction siga;

    siga.sa_handler = child_wait;
    siga.sa_flags  = 0;
    memset (&siga.sa_mask, 0, sizeof(sigset_t));
    sigaction (SIGCHLD, &siga, NULL);

    OnNewDelClient = on_new_del_client;

    if (!ServerStartup (0 , 0 , 0)) {
        fprintf (stderr, 
                 "Can not start the server of MiniGUI-Processes: mginit.\n");
        return 1;
    }


    SetServerEventHook (my_event_hook);

    if (args > 1) {
        if (exec_app (arg[1], arg[1]) == 0)
            return 3;
    }

    while (!quit && GetMessage (&msg, HWND_DESKTOP)) {
        DispatchMessage (&msg);
    }
    return 0;
}
