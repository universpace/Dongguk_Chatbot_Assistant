# -*- coding: utf-8 -*-
import requests
import json
import urllib3
import datetime

from flask import Flask, request, make_response, jsonify, render_template
from bs4 import BeautifulSoup
from urllib.request import urlopen



ERROR_MESSAGE = '네트워크 접속에 문제가 발생하였습니다. 잠시 후 다시 시도해주세요.'
URL_OPEN_TIME_OUT = 10

app = Flask(__name__)





#----------------------------------------------------
# Dialogflow에서 대답 구함
#----------------------------------------------------
def get_answer(text, user_key):
    
    #--------------------------------
    # Dialogflow에 요청
    #--------------------------------
    data_send = { 
        'lang': 'ko',
        'query': text,
        'sessionId': user_key,
        'timezone': 'Asia/Seoul'
    }
    
    data_header = {
        'Content-Type': 'application/json; charset=utf-8',
        'Authorization': 'b5a055ab32ed4e3b8f4a1a5bb54701fb  '	# Dialogflow의 Client access token 입력
    }
    
    dialogflow_url = 'https://api.dialogflow.com/v1/query?v=20150910'
    
    res = requests.post(dialogflow_url,
                            data=json.dumps(data_send),
                            headers=data_header)

    #--------------------------------
    # 대답 처리
    #--------------------------------
    if res.status_code != requests.codes.ok:
        return ERROR_MESSAGE
    
    data_receive = res.json()
    answer = data_receive['result']['fulfillment']['speech'] 
    
    return answer



#----------------------------------------------------
# 이동경로 처리
#----------------------------------------------------
def Route(now, destination):
    
    f = open("route.txt", 'r')
    lines = f.readlines()
    for line in lines:
        item = line.split(",")
        if now == item[item.index("now")+1] and destination == item[item.index("destination")+1]:
            answer = item[item.index("answer")+1]
            break
            
    return answer


#----------------------------------------------------
# 학식 메뉴 처리
#----------------------------------------------------
def Menu(address):
    html = urlopen('http://dgucoop.dongguk.edu/store/store.php?w=4&l=2&j=0')
    source = html.read()
    html.close()

    n = datetime.datetime.today().weekday()

    soup = BeautifulSoup(source, "lxml")
    table_div = soup.find(id="sdetail")
    tables = table_div.find_all("table")

    menu_table = tables[1]

    trs = menu_table.find_all('tr')

    if address == u'상록원':
        baik_lunch = trs[8]
        baik_dinner = trs[9]

        bkbLn = baik_lunch.find_all('td')
        if not bkbLn[n+3].span.get_text() :
            baik_menu = '백반코너 중식 :\n없음\n석식 : \n'
        else :
            baik_menu = '백반코너 중식 :\n' + bkbLn[n+3].span.get_text() + '\n석식 :\n'
        
        bkbDn = baik_dinner.find_all('td')
        if not bkbDn[n+2].span.get_text():
            baik_menu +='없음 \n\n'
        else :
            baik_menu += bkbDn[n+2].span.get_text() + '\n\n'


        ilpoom_lunch = trs[10]
        ilpoom_dinner = trs[11]

        ilpLn = ilpoom_lunch.find_all('td')
        if not ilpLn[n+3].span.get_text() :
            ilp_menu = '일품코너 중식 :\n없음\n석식 : \n'
        else :
            ilp_menu = '일품코너 중식 :\n' + ilpLn[n+3].span.get_text() + '\n석식 :\n'
        
        ilpDn = ilpoom_dinner.find_all('td')
        if not ilpDn[n+2].span.get_text():
           ilp_menu +='없음 \n'
        else :
            ilp_menu += ilpDn[n+2].span.get_text() + '\n\n'
            
        western_Lunch = trs[12]
        western_Dinner = trs[13]

        wstLn = western_Lunch.find_all('td')
        if not wstLn[n+3].span.get_text():
            wst_menu = '양식코너 중식 :\n없음\n석식 :\n'    
        else : 
            wst_menu = '양식코너 중식 :\n' +  wstLn[n+3].span.get_text() + '\n석식 :\n'
        
        wstDn = western_Dinner.find_all('td')
        if not wstDn[n+2].span.get_text():
            wst_menu +='없음 \n\n'
        else :
           wst_menu  += wstDn[n+2].span.get_text()+'\n\n'
        
        ddk_Lunch = trs[14]
        ddk_Dinner = trs[15]

        ddkLn = ddk_Lunch.find_all('td')
        if not ddkLn[n+3].span.get_text():
            ddk_menu = '뚝배기 코너 중식 :\n없음\n석식 :\n'
        else : 
            ddk_menu = '뚝배기코너 중식 :\n' +  ddkLn[n+3].span.get_text() + '\n석식 :\n'
        
        ddkDn = ddk_Dinner.find_all('td')
        if not ddkDn[n+2].span.get_text():
            ddk_menu +='없음 \n\n'
        else :
           ddk_menu  += ddkDn[n+2].span.get_text() +'\n\n'
    
        answer = '상록원 메뉴정보입니다.맛있는 식사 되세요! \n' + baik_menu + ilp_menu + wst_menu + ddk_menu

    elif address == u'그루터기':
        answer = '현재 그루터기는 학기 종료로 인한 영업 종료 상태입니다.'
        '''
      
        A_lunch = trs[25]
        A_dinner = trs[26]

        ALn = A_lunch.find_all('td')
        if not ALn[n+3].span.get_text():
            A_menu = 'A코너 중식 : 없음, 석식 : '
        else :
            A_menu = 'A코너 중식 : ' + ALn[n+3].span.get_text() + ', 석식 : '
        
        ADn = A_dinner.find_all('td')
        if not ADn[n+2].span.get_text():
            A_menu +='없음 \n'
        else :
            A_menu += ADn[n+2].span.get_text() + '\n'   

        B_lunch = trs[27]
        B_dinner = trs[28]

        BLn = B_lunch.find_all('td')
        if not BLn[n+3].span.get_text():
            B_menu = 'B코너 중식 : 없음, 석식 : '
        else :
            B_menu = 'B코너 중식 : ' + ALn[n+3].span.get_text() + ', 석식 : '
        
        BDn = B_dinner.find_all('td')
        if not ADn[n+2].span.get_text():
            B_menu +='없음 \n'
        else :
            B_menu += ADn[n+2].span.get_text() + '\n'
    
        answer = '그루터기 메뉴 정보입니다. 맛있게 드세요! \n' + A_menu + B_menu         
        '''
        
    elif address == u'기숙사식당' or address == u'긱식' or address == u'기숙사 식당':
        
        Morning = trs[33]
        A_lunch = trs[34]
        A_dinner = trs[35]
        
        Mor = Morning.find_all('td')
        if not Mor[n+2].span.get_text():
            Dorm_menu = '\n조식 : \n없음\n중식 : \n'
        else :
            Dorm_menu = '\n조식 : \n' + Mor[n+2].span.get_text() + '\n중식 : \n'

        ALn = A_lunch.find_all('td')
        if not ALn[n+3].span.get_text():
            Dorm_menu += '없음\n석식 : \n'
        else :
            Dorm_menu += ALn[n+3].span.get_text() + '\n석식 : \n'
        
        ADn = A_dinner.find_all('td')
        if not ADn[n+2].span.get_text():
            Dorm_menu +='\n없음 \n'
        else :
            Dorm_menu += ADn[n+2].span.get_text() + '\n'   

    
        answer = '기숙사식당 메뉴 정보입니다. 맛있게 드세요! \n' + Dorm_menu     
    return answer



#------------------------------------'/'
# Dialogflow fullfillment 처리
#------------------------------------'/'
@app.route('/', methods=['POST'])
def webhook():

    #--------------------------------'/'
    # 액션 구함
    #--------------------------------
    req = request.get_json(force=True)
    action = req['result']['action']

    #--------------------------------
    # 액션 처리
    #--------------------------------
    if action == 'CafeterriaLocation':
        address = req['result']['parameters']['dining_place']
        answer = Menu(address)
    elif action == 'RouteRequest':
        now = req['result']['parameters']['DG_Locate']
        address = req['result']['parameters']['DG_Locate1']
        answer = Route(now, address)
    elif action == 'help':
        answer = '제가 할 수 있는 질문 목록이에요\n'
        answer += '1.다향관에 대한 질문(ex.사진관갈래, 안경 부러졌어, 책 살래, 다향관에 대해 알려줘)\n'
        answer += '2. CS센터에 대한 질문(ex.CS센터에는 뭐가 있어, 비가오는데 우산이 없어, 학생증 재발급 받고싶어)\n'
        answer += '3. 식당 메뉴(ex. 상록원 메뉴 뭐야, 학식 메뉴, 기숙사식당 메뉴, 그루터기 메뉴)\n'
        answer += '4. 길찾기 서비스( 길찾기 서비스 라고 말한 뒤 현위치에서 목적지 입력하면 되요. ex. 신공학관에서 학생회관)\n'
    else:
        answer = 'error'

    res = {'speech': answer}
        
    return jsonify(res)





#----------------------------------------------------
# 메인 함수
#----------------------------------------------------
if __name__ == '__main__':

    app.run(host = '0.0.0.0', port = 5000, threaded = True, debug=True)  
