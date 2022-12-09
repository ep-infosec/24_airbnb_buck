package cgo

/*
#include <lib_gen.h>
#include <stdlib.h>

void wrapper(int i) {
  print_int(i);
}
*/
import "C"

import (
	"fmt"
)

func Test() {
	C.simple_hello()

	C.print_int(5)

	C.wrapper(6)

	var i int = 8
	C.print_int(C.int(i))

	cs := C.CString("Go string")
	csRet := C.complex_func(cs)
	fmt.Printf("fmt: %s\\n", C.GoString(csRet))
}
