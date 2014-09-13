
/* List procedures with semantics similar to Scheme lists */

(define null
	(lambda ()
		(lambda (op)
			(if 
				(= op 0)  1                //op value 0, null? - Yes
				(if 
					(= op 1) 0             //op value 1, pair? - No
					undefined              //other operators are undefined.
				)
			)
		) 
	)
)

(define cons
	(lambda (fst snd)
		(lambda (op)
			(if 
				(= op 0)  0                //op value 0, null? - No
				(if 
					(= op 1) 1             //op value 1, pair? - Yes
					(if 
						(= op 2) fst       //op value 2, car 
						(if 
							(= op 3) snd   //op value 3, cdr 
							undefined
						)
					)
				)
			)
		) 
	)
)

(define isnull
	(lambda (obj)
		(= (obj 0) 1)
	)
)

(define ispair
	(lambda (obj)
		(= (obj 1) 1)
	)
)

(define car
	(lambda (pair)
		(if (ispair pair) (pair 2)       // Is it a pair?
			carNotDefinedForNonPair
		)
	)
)

(define cdr 
	(lambda (pair)
		(if (ispair pair) (pair 3)       // Is it a pair?
			carNotDefinedForNonPair
		)
	)
)

(define length
	(lambda (lst)
		(if (isnull lst) 0
			(+ 1 (length (cdr lst)))
		)		
	)
)

(define append
	(lambda (lst1 lst2)
		(if (isnull lst1) lst2
			(if (isnull lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)

(define list
	(lambda (elem)
		(cons elem (null))
	)
)

