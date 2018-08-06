package main

import (
	"fmt"
	"log"
	"net/http"
	"os/exec"
	//"strings"
)

func exec_shell(s string) {
	cmd := exec.Command("/bin/bash", "-c", s)
	err := cmd.Run()
	fmt.Println(err)
}

func sayhelloName(w http.ResponseWriter, r *http.Request) {
	exec_shell("./opt/mongoclone.sh")
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
	fmt.Fprintf(w, "run mongoclone")
}
func main() {
	http.HandleFunc("/hello", sayhelloName)
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
