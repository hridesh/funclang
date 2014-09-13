
/* List procedures with semantics similar to Scheme lists */

(define null
	(lambda ()
		(lambda (op)
			(if 
				(= op 0)  #t                //op value 0, null? - Yes
				(if 
					(= op 1) #f             //op value 1, pair? - No
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
				(= op 0)  #f                //op value 0, null? - No
				(if 
					(= op 1) #t             //op value 1, pair? - Yes
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

(define null?
	(lambda (obj)
		(obj 0)
	)
)

(define pair?
	(lambda (obj)
		(obj 1)
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

(define cadr
	(lambda (lst)
		(car (cdr lst))
	)
)

(define caddr
	(lambda (lst)
		(car (cdr (cdr lst)))
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

(define map
	(lambda (op lst)
		(if (isnull lst) (null)
			(cons (op (car lst)) (map (cdr lst)))
		)		
	)
)

(define list
	(lambda (elem)
		(cons elem (null))
	)
)

