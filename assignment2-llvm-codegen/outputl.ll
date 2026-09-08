    %1 = 123
    %i = %1
    %2 = 12
    %d = %2
    %3 = "123 "
    %s = %3
    %4 = add i32 %i, %d
    %5 = 1
    %6 = 2
    %7 = icmp eq i32 %5, %6
    %8 = 0
    %min = %8
    %9 = 10
    %max = %9
    %10 = add i32 %min, %max
    %temp = %10
    %11 = 2
    %12 = sdiv i32 %temp, %11
    %average = %12
    %13 = 0
    %x = %13
    %14 = icmp eq i32 %i, %s
    br i1 %14, label %L1, label %L2
L1:
    %15 = 1
    %x = %15
    br label %L3
L2:
    %16 = 2
    %x = %16
    br label %L3
L3:
    %17 = 1
    %a = %17
    br label %L4
L4:
    %18 = 10
    %19 = icmp slt i32 %a, %18
    br i1 %19, label %L5, label %L6
L5:
    %20 = 5
    %21 = mul i32 %x, %20
    %x = %21
    %22 = 1
    %23 = add i32 %a, %22
    %a = %23
    br label %L4
L6:
