
newImage("Untitled", "8-bit White", 400, 400, 10);
run("Label...", "format=0 starting=1 interval=1 x=20 y=20 font=18 text=[] range=1-10");

a = 8;
f();
print("a outside f= " + a);

function f(){
	a = 3;	
	run("Slice Remover", "first=&a last=&a increment=1"); //frame 8 gets removed, not 3!!!
	print("a inside f= " + a);
}
