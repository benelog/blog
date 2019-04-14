DECLARE SUB display (c$, p$)

SCREEN 2
lg$ = " Code can be an art! "
DATA "t90O1p4","e8","a8","b8",">c+8","g8","f8","<a8"
DATA ">d8","c+8","<e2","e8","a4","b8",">c+4","<b8","a2","e2","p4","p4","p4","p4"
FOR i = 1 TO LEN(lg$)
    READ p$
    display MID$(lg$, i, 1), p$
NEXT i
SCREEN 0
END

SUB display (c$, p$)
PLAY p$
CLS
PRINT c$
FOR i = 0 TO 10
  FOR j = 0 TO 10
    IF POINT(j, i) THEN LINE (j * 60 + 80, i * 25 + 10)-(j * 60 + 140, i * 25 + 40), 1, BF
  NEXT j
NEXT i
LOCATE 1, 1
PRINT " "

END SUB

