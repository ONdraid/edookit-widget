from bs4 import BeautifulSoup
import requests


def main(username, password, school_id):
    login_url = 'https://' + school_id + '.edookit.net/user/login'
    login_check_url = 'https://' + school_id + '.edookit.net/user/login?do=loginForm-checkCaptcha&loginForm-username=' + username
    timetable_url = 'https://' + school_id + '.edookit.net/timetable/?do=familyTimetable-resetFilter'


    payload = {
        'username': username,
        'password': password,
        'g-recaptcha-response': '',
        'send': 'Přihlásit',
        'remember': '',
        '_do': 'loginForm-form-submit'
    }

    try:
        with requests.session() as s:
            s.get(login_check_url)
            s.post(login_url, data=payload)
            r = s.get(timetable_url)
    except:
        return "network_error"

    try:
        soup = BeautifulSoup(BeautifulSoup(r.text, 'html.parser').find(class_='timetable-container').prettify(), 'html.parser')
        soup.find(class_='timetable-container')['style'] = 'max-width:900px;display: block'
        for e in soup.select('script'):
            e.extract()
        timetable_grab = '<meta name="viewport" content="width=device-width, initial-scale=1">\n' + soup.prettify().replace('\n        min-width: 1258px;', '')
        return timetable_grab
    except:
        return "error"

