// You can edit this code!
// Click here and start typing.
package main

import (
	"fmt"
	"strconv"

	"github.com/dropbox/dropbox-sdk-go-unofficial/dropbox"
	"github.com/dropbox/dropbox-sdk-go-unofficial/dropbox/files"
)

func main() {

	config := dropbox.Config{
		Token:    "",
		LogLevel: dropbox.LogInfo, // if needed, set the desired logging level. Default is off
	}
	/* 	dbx := users.New(config) */

	lfob := files.NewListFolderArg("/приложения/WahooFitness")

	filesdb := files.New(config)

	lf, err := filesdb.ListFolder(lfob)

	if err != nil {
		return
	}

	fmt.Printf(strconv.FormatBool(lf.HasMore))

	return
	/*arg := users.NewGetAccountArg(accountId)
	if resp, err := dbx.GetAccount(arg); err != nil {
		return err
	}
	fmt.Printf("Name: %v", resp.Name)
	*/
}
