from bs4 import BeautifulSoup
from os.path import dirname, join
from com.chaquo.python import Python
from html2image import Html2Image
import requests


def main(username, password, school_id):
    login_url = 'https://' + school_id + '.edookit.net/user/login'
    login_check_url = 'https://' + school_id + '.edookit.net/user/login?do=loginForm-checkCaptcha&loginForm-username=' + username
    timetable_url = 'https://' + school_id + '.edookit.net/timetable/?do=familyTimetable-resetFilter'

    files_dir = str(Python.getPlatform().getApplication().getFilesDir())
    file_name = join(dirname(files_dir), 'timetable.jpg')

    payload = {
        'username': username,
        'password': password,
        'g-recaptcha-response': '',
        'send': 'Přihlásit',
        'remember': '',
        '_do': 'loginForm-form-submit'
    }

    with requests.session() as s:
        s.get(login_check_url)
        s.post(login_url, data=payload)
        r = s.get(timetable_url)
    try:
        soup = BeautifulSoup(r.text, 'html.parser')
        soup.find(class_='timetable')['style'] = 'background-color:white;'
        timetable_grab = '<html>\n<body>\n' + soup.find(class_='timetable').prettify() + '</body>\n</html>'
        pass
    except:
        timetable_grab = "error"
        pass

    # hti = Html2Image(output_path=files_dir)
    # hti.screenshot(html_str=timetable_grab, size=(900, 350), save_as='timetable.jpg')

    return timetable_grab

