"Broad-based categorical eligibility (BBCE) is a policy that makes most households categorically eligible for SNAP because they qualify for a non-cash Temporary Assistance for Needy Families (TANF) or State maintenance of effort (MOE) funded benefit. The chart below shows which States implemented BBCE, the programs that confer BBCE, the asset limit of the TANF/MOE program, and the gross income limit of the TANF/MOE program.
BBCE cannot limit eligibility. Households with seniors or disabled members that are not eligible for the program that confers categorical eligibility may apply for and receive SNAP under regular SNAP rules. Under regular program rules, households with elderly or disabled members do not need to meet the gross income limit, but must meet the net income limit."			 
    ** convert to int if comparing numbers is better

c = case_number
    
    if one number, get last number of case and check if ==
    else compare (either convert to int or compare as string;
        not sure how comparing numbers as string will work)
        ** special case for if number end in 0 or 9!
mc = multiple case_numbers to compate to
    
    compare last digits of case number to all case number
    
n = none

    get benefits first date
    
s = ssn
    
    if one number, get last number of case and check if ==
        else compare (either convert to int or compare as string;
            not sure how comparing numbers as string will work)
            
ms = multiple ssns to compate to
    
    compare last digits of ssn to all ssn
            
l = last_name // store in all caps
    
    if one thing to compare to, check if substring == thing
    else use compare to with two ends
    
ml = multiple last names

    check if substring is equal to each of the individual conditions
    
e = 9th + 8th number of case
    
    drop last number
    get number in reverse order for easy comparison
    
v = 7th digit of case number
    
      if one number, get last number of case and check if ==
      else compare (either convert to int or compare as string;
          not sure how comparing numbers as string will work)


y = last digit of birthyear


d = last digit of birthday
    
    if one number, get last number of case and check if ==
    else compare to each individual number

j = birthday month and last name
    
    get month number and first letter of last name
    see if month == and if letter is in between
    