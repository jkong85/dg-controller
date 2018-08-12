package main

import (
	"fmt"
	"log"
	"net/http"
	"os/exec"
	"strings"
)

/*
To cross-compile and run on Container:
	env GOOS=linux GOARCH=amd64 go build  http.go
*/

func exec_shell(s string) {
	cmd := exec.Command("/bin/bash", "-c", s)
	err := cmd.Run()
	fmt.Println(err)
}

func cloneMongo(w http.ResponseWriter, r *http.Request) {
	exec_shell("./opt/mongoclone.sh")
	r.ParseForm()
	for k, v := range r.Form {
		fmt.Println("key:", k)
		fmt.Println("val:", strings.Join(v, ""))
		if k == "ip" {
			ipaddress := strings.Join(v, "")
			exec_shell("./opt/mongoclone.sh " + ipaddress)
		}
	}

	/*
		r.ParseForm()
		fmt.Println(r.Form)
		fmt.Println("path", r.URL.Path)
		fmt.Println("scheme", r.URL.Scheme)
		fmt.Println(r.Form["url_long"])
		for k, v := range r.Form {
			fmt.Println("key:", k)
			fmt.Println("val:", strings.Join(v, ""))
		}
	*/
	fmt.Fprintf(w, "Run mongoclone ...")
}
func main() {
	http.HandleFunc("/hello", cloneMongo)
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
